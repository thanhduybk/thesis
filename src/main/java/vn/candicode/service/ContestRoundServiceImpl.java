package vn.candicode.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.candicode.entity.ChallengeEntity;
import vn.candicode.entity.ContestEntity;
import vn.candicode.entity.ContestRoundEntity;
import vn.candicode.exception.PersistenceException;
import vn.candicode.exception.ResourceNotFoundException;
import vn.candicode.payload.request.NewRoundRequest;
import vn.candicode.payload.request.UpdateRoundRequest;
import vn.candicode.repository.ChallengeRepository;
import vn.candicode.repository.ContestRepository;
import vn.candicode.repository.ContestRoundRepository;
import vn.candicode.security.UserPrincipal;
import vn.candicode.util.DatetimeUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Log4j2
public class ContestRoundServiceImpl implements ContestRoundService {
    private final ContestRoundRepository contestRoundRepository;
    private final ContestRepository contestRepository;
    private final ChallengeRepository challengeRepository;

    public ContestRoundServiceImpl(ContestRoundRepository contestRoundRepository, ContestRepository contestRepository, ChallengeRepository challengeRepository) {
        this.contestRoundRepository = contestRoundRepository;
        this.contestRepository = contestRepository;
        this.challengeRepository = challengeRepository;
    }

    @Override
    @Transactional
    public void createRound(Long contestId, NewRoundRequest payload, UserPrincipal me) {
        ContestEntity contest = contestRepository.findByContestIdFetchRounds(contestId)
            .orElseThrow(() -> new ResourceNotFoundException(ContestEntity.class, "id", contestId));

        ContestRoundEntity round = new ContestRoundEntity();

        round.setName("Round " + contest.getRounds().size() + 1);

        LocalDateTime startsAt = LocalDateTime.parse(payload.getStartsAt(), DatetimeUtils.JSON_DATETIME_FORMAT);
        LocalDateTime endsAt = LocalDateTime.parse(payload.getEndsAt(), DatetimeUtils.JSON_DATETIME_FORMAT);
        round.setStartsAt(startsAt);
        round.setDuration(ChronoUnit.MINUTES.between(startsAt, endsAt));

        List<ChallengeEntity> roundChallenges = challengeRepository.findAllContestChallengeByChallengeIdIn(payload.getChallenges());

        if (roundChallenges.size() != payload.getChallenges().size()) { // Guarantee that all challenges is contest challenge and existing
            throw new PersistenceException("Some Contest challenge(s) not found");
        } else {
            roundChallenges.forEach(round::addChallenge);
        }

        contest.addRound(round);
    }

    @Override
    public void updateRound(Long roundId, UpdateRoundRequest payload, UserPrincipal me) {
        ContestRoundEntity round = contestRoundRepository.findByRoundIdFetchChallenges(roundId)
            .orElseThrow(() -> new ResourceNotFoundException(ContestRoundEntity.class, "id", roundId));

        if (!round.getName().equals(payload.getName())) {
            round.setName(payload.getName());
        }

        LocalDateTime startsAt = LocalDateTime.parse(payload.getStartsAt(), DatetimeUtils.JSON_DATETIME_FORMAT);
        LocalDateTime endsAt = LocalDateTime.parse(payload.getEndsAt(), DatetimeUtils.JSON_DATETIME_FORMAT);
        round.setStartsAt(startsAt);
        round.setDuration(ChronoUnit.MINUTES.between(startsAt, endsAt));

        List<Long> existingChallengeIds = round.getChallenges().stream()
            .map(item -> item.getChallenge().getChallengeId())
            .collect(Collectors.toList());

        List<Long> newChallengeIds = payload.getChallenges().stream()
            .filter(c -> !existingChallengeIds.contains(c))
            .collect(Collectors.toList());

//        existingChallengeIds.stream()
//            .filter(c -> !payload.getChallenges().contains(c))
//            .forEach(c -> round.getChallenges().remove()); // DOING

        List<ChallengeEntity> roundChallenges = challengeRepository.findAllByContestChallengeByChallengeIdIn(payload.getChallenges());

    }

    @Override
    @Transactional
    public void removeRound(Long roundId, UserPrincipal me) {
        ContestRoundEntity round = contestRoundRepository.findByRoundIdFetchChallenges(roundId)
            .orElseThrow(() -> new ResourceNotFoundException(ContestRoundEntity.class, "id", roundId));

        round.setDeleted(true);
    }
}