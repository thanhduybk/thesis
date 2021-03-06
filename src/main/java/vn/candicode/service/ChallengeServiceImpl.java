package vn.candicode.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import vn.candicode.common.EntityConstants;
import vn.candicode.core.StorageService;
import vn.candicode.entity.ChallengeConfigurationEntity;
import vn.candicode.entity.ChallengeEntity;
import vn.candicode.exception.BadRequestException;
import vn.candicode.exception.PersistenceException;
import vn.candicode.exception.ResourceNotFoundException;
import vn.candicode.exception.StorageException;
import vn.candicode.payload.request.NewChallengeRequest;
import vn.candicode.payload.request.UpdateChallengeRequest;
import vn.candicode.payload.response.ChallengeDetails;
import vn.candicode.payload.response.ChallengeSummary;
import vn.candicode.payload.response.DirectoryTree;
import vn.candicode.payload.response.PaginatedResponse;
import vn.candicode.payload.response.sub.Challenge;
import vn.candicode.payload.response.sub.Testcase;
import vn.candicode.repository.ChallengeConfigurationRepository;
import vn.candicode.repository.ChallengeRepository;
import vn.candicode.security.UserPrincipal;
import vn.candicode.util.ChallengeBeanUtils;
import vn.candicode.util.FileUtils;
import vn.candicode.util.RegexUtils;

import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static vn.candicode.common.FileStorageType.BANNER;
import static vn.candicode.common.FileStorageType.CHALLENGE;

@Service
@Log4j2
public class ChallengeServiceImpl implements ChallengeService {
    private final ChallengeRepository challengeRepository;
    private final ChallengeConfigurationRepository challengeConfigurationRepository;

    private final StorageService storageService;
    private final CommonService commonService;

    @PersistenceContext
    private EntityManager entityManager;

    public ChallengeServiceImpl(ChallengeRepository challengeRepository, ChallengeConfigurationRepository challengeConfigurationRepository, StorageService storageService, CommonService commonService) {
        this.challengeRepository = challengeRepository;
        this.challengeConfigurationRepository = challengeConfigurationRepository;

        this.storageService = storageService;
        this.commonService = commonService;
    }

    /**
     * @param payload
     * @param author
     * @return id of new challenge
     * @throws StorageException
     * @throws PersistenceException
     */
    @Override
    @Transactional
    public Long createChallenge(NewChallengeRequest payload, UserPrincipal author) {
        Long authorId = author.getUserId();
        try {
            String bannerPath;

            if (payload.getBanner() == null || payload.getBanner().isEmpty()) {
                bannerPath = null;
            } else {
                bannerPath = storageService.store(payload.getBanner(), BANNER, authorId);
            }

            ChallengeEntity challenge = new ChallengeEntity();

            challenge.setTitle(payload.getTitle());
            challenge.setDescription(payload.getDescription());
            challenge.setInputFormat(RegexUtils.genRegex(payload.getTcInputFormat()));
            challenge.setOutputFormat(RegexUtils.genRegex(payload.getTcOutputFormat()));
            challenge.setLevel(payload.getLevel().toLowerCase());
            challenge.setMaxPoint(challenge.getLevel());
            challenge.setAuthor(author.getEntityRef());
            challenge.setBanner(storageService.simplifyPath(bannerPath, BANNER, authorId));
            challenge.setTags(payload.getTags());
            challenge.setContestChallenge(payload.getContestChallenge());

            if (payload.getCategories() != null) {
                payload.getCategories().forEach(e -> {
                    if (commonService.getCategories().containsKey(e)) {
                        challenge.addCategory(commonService.getCategories().get(e));
                    }
                });
            }

            entityManager.persist(challenge);

            ChallengeConfigurationEntity challengeConfig = new ChallengeConfigurationEntity();

            challengeConfig.setChallenge(challenge);

            String language = payload.getLanguage().toLowerCase();

            if (!commonService.getLanguages().containsKey(language)) {
                throw new PersistenceException("No language with name '" + payload.getLanguage() + "' found");
            }

            challengeConfig.setLanguage(commonService.getLanguages().get(language));
            challengeConfig.setDirectory(payload.getChallengeDir());
            challengeConfig.setPreImplementedFile(storageService.simplifyPath(payload.getImplementedPath(), CHALLENGE, authorId));
            challengeConfig.setNonImplementedFile(storageService.simplifyPath(payload.getNonImplementedPath(), CHALLENGE, authorId));
            challengeConfig.setRunScript(storageService.simplifyPath(payload.getRunPath(), CHALLENGE, authorId));
            challengeConfig.setAuthorId(authorId);

            /*
             * Root dir is the folder that the run script is placed in
             * */
            String rootDir = Paths.get(payload.getRunPath()).getParent().toString();
            challengeConfig.setRoot(storageService.simplifyPath(rootDir, CHALLENGE, authorId));

            if (payload.getCompilePath() != null) {
                challengeConfig.setCompileScript(storageService.simplifyPath(payload.getCompilePath(), CHALLENGE, authorId));
            }

            entityManager.persist(challengeConfig);

            return challenge.getChallengeId();
        } catch (IOException e) {
            log.error("I/O error. Message - {}", e.getLocalizedMessage());
            throw new StorageException(e.getLocalizedMessage());
        } catch (EntityExistsException e) {
            log.error("Entity has already existing. Message - {}", e.getLocalizedMessage());
            throw new PersistenceException(e.getLocalizedMessage());
        }
    }

    /**
     * @param file must be a zip file
     * @param me
     * @return
     * @throws StorageException
     */
    @Override
    public DirectoryTree storeChallengeSource(MultipartFile file, UserPrincipal me) {
        try {
            String challengeDir = storageService.store(file, CHALLENGE, me.getUserId());
            String challengeDirname = challengeDir.substring(challengeDir.lastIndexOf(File.separator) + 1);

            DirectoryTree tree = new DirectoryTree();
            tree.setChallengeDir(challengeDirname);
            tree.setChildren(storageService.parse(challengeDir));

            return tree;
        } catch (IOException e) {
            log.error("I/O error. Message - {}", e.getLocalizedMessage());
            throw new StorageException(e.getLocalizedMessage());
        }
    }

    /**
     * TODO: Optimize SQL Query
     *
     * @param pageable
     * @return paginated list of challenges
     */
    @Override
    @Transactional(readOnly = true)
    public PaginatedResponse<ChallengeSummary> getChallengeList(Pageable pageable) {
        Page<ChallengeEntity> items = challengeRepository.findAllFetchLanguages(pageable);

        List<ChallengeSummary> summaries = items.map(ChallengeBeanUtils::summarize).getContent();

        return PaginatedResponse.<ChallengeSummary>builder()
            .first(items.isFirst())
            .last(items.isLast())
            .page(items.getNumber())
            .size(items.getSize())
            .totalElements(items.getTotalElements())
            .totalPages(items.getTotalPages())
            .items(summaries)
            .build();
    }

    /**
     * @param pageable
     * @param myId
     * @param wantContestChallenge should load only contest challenge ?
     * @return paginated list of my challenges
     */
    @Transactional(readOnly = true)
    @Override
    public PaginatedResponse<ChallengeSummary> getMyChallengeList(Pageable pageable, Long myId, Boolean wantContestChallenge) {
        Page<ChallengeEntity> items;
        if (wantContestChallenge) {
            items = challengeRepository.findAllContestChallengesByAuthorId(myId, pageable);
        } else {
            items = challengeRepository.findAllByAuthorId(myId, pageable);
        }

        List<ChallengeSummary> summaries = items.map(ChallengeBeanUtils::summarize).getContent();

        return PaginatedResponse.<ChallengeSummary>builder()
            .first(items.isFirst())
            .last(items.isLast())
            .page(items.getNumber())
            .size(items.getSize())
            .totalElements(items.getTotalElements())
            .totalPages(items.getTotalPages())
            .items(summaries)
            .build();
    }

    /**
     * @param challengeId
     * @return details of challenge with given id
     */
    @Override
    @Transactional(readOnly = true)
    public ChallengeDetails getChallengeDetails(Long challengeId, UserPrincipal me) {
        ChallengeEntity challenge = challengeRepository.findByChallengeIdFetchTestcases(challengeId)
            .orElseThrow(() -> new ResourceNotFoundException(ChallengeEntity.class, "id", challengeId));

        List<ChallengeConfigurationEntity> challengeConfigurations = challengeConfigurationRepository
            .findAllByChallengeIdFetchLanguage(challengeId);

        ChallengeDetails details = ChallengeBeanUtils.details(challenge);

        challenge.getTestcases().stream()
            .map(item -> new Testcase(item.getTestcaseId(), item.getInput(), item.getExpectedOutput(), item.getHidden(), me != null && challenge.getAuthor().getUserId().equals(me.getUserId())))
            .forEach(item -> details.getTestcases().add(item));

        details.setContents(challengeConfigurations.stream()
            .map(config -> new Challenge(config.getLanguage().getName(), FileUtils.readFileToString(new File(storageService.resolvePath(config.getNonImplementedFile(), CHALLENGE, challenge.getAuthor().getUserId())))))
            .collect(Collectors.toList()));

        return details;
    }

    /**
     * Only author can edit challenge
     *
     * @param challengeId
     * @param payload
     * @param me
     */
    @Override
    public void updateChallenge(Long challengeId, UpdateChallengeRequest payload, UserPrincipal me) {
        ChallengeEntity challenge = challengeRepository.findByChallengeIdFetchCategories(challengeId)
            .orElseThrow(() -> new ResourceNotFoundException(ChallengeEntity.class, "id", challengeId));

        if (!challenge.getAuthor().getUserId().equals(me.getUserId())) {
            throw new BadRequestException("You are not the owner of this challenge");
        }

        if (payload.getTitle() != null && !challenge.getTitle().equals(payload.getTitle())) {
            if (challengeRepository.existsByTitle(payload.getTitle())) {
                throw new PersistenceException("Challenge has been already exist with tile" + payload.getTitle());
            }
            challenge.setTitle(payload.getTitle());
        }

        if (payload.getLevel() != null && !challenge.getLevel().equals(payload.getLevel()) && EntityConstants.LEVELS.contains(payload.getLevel())) {
            challenge.setLevel(payload.getLevel());
            challenge.setMaxPoint(payload.getLevel());
        }

        if (payload.getDescription() != null) {
            challenge.setDescription(payload.getDescription());
        }

        if (payload.getTags() != null) {
            challenge.setTags(payload.getTags());
        }

        if (payload.getContestChallenge() != null) {
            challenge.setContestChallenge(payload.getContestChallenge());
        }

        if (payload.getCategories() != null) {
            Set<String> existingCategories = !challenge.getCategories().isEmpty()
                ? challenge.getCategories().stream()
                .map(c -> c.getCategory().getName()).collect(Collectors.toSet())
                : new HashSet<>();

            Set<String> newCategories = payload.getCategories().stream()
                .filter(c -> !existingCategories.contains(c))
                .collect(Collectors.toSet());

            existingCategories.stream()
                .filter(c -> !payload.getCategories().contains(c)) // Filter existing categories that not be included in this update
                .forEach(c -> {
                    if (commonService.getCategories().containsKey(c)) {
                        challenge.removeCategory(commonService.getCategories().get(c));
                    }
                }); // Remove them

            newCategories.forEach(c -> {
                if (commonService.getCategories().containsKey(c)) {
                    challenge.addCategory(commonService.getCategories().get(c));
                }
            });
        }

        if (payload.getBanner() != null && !payload.getBanner().isEmpty()) {
            try {
                String bannerPath = storageService.store(payload.getBanner(), BANNER, me.getUserId());
                storageService.delete(challenge.getBanner(), BANNER, me.getUserId());
                challenge.setBanner(storageService.simplifyPath(bannerPath, BANNER, me.getUserId()));
            } catch (IOException ignored) {
            }
        }

        challengeRepository.save(challenge);
    }

    /**
     * <ul>
     *     <li>Only author can delete his challenge</li>
     *     <li>Call this method will only delete the record softly</li>
     * </ul>
     *  @param challengeId
     *
     * @param me
     */
    @Override
    @Transactional
    public void deleteChallenge(Long challengeId, UserPrincipal me) {
        ChallengeEntity challenge = challengeRepository.findByChallengeId(challengeId)
            .orElseThrow(() -> new ResourceNotFoundException(ChallengeEntity.class, "id", challengeId));

        if (!challenge.getAuthor().getUserId().equals(me.getUserId()) || !me.getAuthorities().contains(new SimpleGrantedAuthority("super admin"))) {
            throw new BadRequestException("You are not the owner of this challenge");
        }

        challenge.setDeleted(true);
    }
}
