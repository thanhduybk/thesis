package vn.candicode.service;

import lombok.extern.log4j.Log4j2;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import vn.candicode.common.CommentSubject;
import vn.candicode.entity.*;
import vn.candicode.exception.ResourceNotFoundException;
import vn.candicode.payload.request.NewCommentRequest;
import vn.candicode.payload.response.CommentDetails;
import vn.candicode.payload.response.CommentSummary;
import vn.candicode.payload.response.PaginatedResponse;
import vn.candicode.repository.ChallengeCommentRepository;
import vn.candicode.repository.ChallengeRepository;
import vn.candicode.repository.TutorialCommentRepository;
import vn.candicode.repository.TutorialRepository;
import vn.candicode.security.UserPrincipal;
import vn.candicode.util.CommentBeanUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Log4j2
public class CommentServiceImpl implements CommentService {
    private final ChallengeCommentRepository challengeCommentRepository;
    private final TutorialCommentRepository tutorialCommentRepository;
    private final ChallengeRepository challengeRepository;
    private final TutorialRepository tutorialRepository;

    public CommentServiceImpl(ChallengeCommentRepository challengeCommentRepository, TutorialCommentRepository tutorialCommentRepository, ChallengeRepository challengeRepository, TutorialRepository tutorialRepository) {
        this.challengeCommentRepository = challengeCommentRepository;
        this.tutorialCommentRepository = tutorialCommentRepository;
        this.challengeRepository = challengeRepository;
        this.tutorialRepository = tutorialRepository;
    }

    /**
     * Create a new comment or reply an existing one
     *
     * @param subject   subject type, can be challenge or tutorial
     * @param subjectId id of challenge or tutorial
     * @param payload
     * @param author
     * @return details of new comment
     */
    @Override
    public CommentDetails addComment(CommentSubject subject, Long subjectId, NewCommentRequest payload, UserPrincipal author) {
        if (subject.equals(CommentSubject.CHALLENGE)) {
            return addChallengeComment(subjectId, payload, author);
        } else {
            return addTutorialComment(subjectId, payload, author);
        }
    }

    private CommentDetails addChallengeComment(Long challengeId, NewCommentRequest payload, UserPrincipal me) {
        ChallengeEntity challenge = challengeRepository.findByChallengeIdFetchComments(challengeId)
            .orElseThrow(() -> new ResourceNotFoundException(ChallengeEntity.class, "id", challengeId));

        ChallengeCommentEntity comment = new ChallengeCommentEntity();

        comment.setAuthor(me.getFullName());
        comment.setContent(payload.getContent());
        comment.setLikes(0);
        comment.setDislikes(0);

        Map<Long /*comment_id*/, ChallengeCommentEntity> commentMap = challenge.getComments().stream()
            .collect(Collectors.toMap(CommentEntity::getCommentId, item -> item));

        if (payload.getParentId() != null && commentMap.containsKey(payload.getParentId())) {
            comment.setParent(commentMap.get(payload.getParentId()));
        }

        challenge.addComment(comment);

        challengeCommentRepository.save(comment);

        return CommentBeanUtils.details(comment);
    }

    private CommentDetails addTutorialComment(Long tutorialId, NewCommentRequest payload, UserPrincipal me) {
        TutorialEntity tutorial = tutorialRepository.findByTutorialIdFetchComments(tutorialId)
            .orElseThrow(() -> new ResourceNotFoundException(TutorialEntity.class, "id", tutorialId));

        TutorialCommentEntity comment = new TutorialCommentEntity();

        comment.setAuthor(me.getFullName());
        comment.setContent(payload.getContent());
        comment.setLikes(0);
        comment.setDislikes(0);

        Map<Long /*comment_id*/, TutorialCommentEntity> commentMap = tutorial.getComments().stream()
            .collect(Collectors.toMap(CommentEntity::getCommentId, item -> item));

        if (payload.getParentId() != null && commentMap.containsKey(payload.getParentId())) {
            comment.setParent(commentMap.get(payload.getParentId()));
        }

        tutorial.addComment(comment);

        tutorialCommentRepository.save(comment);

        return CommentBeanUtils.details(comment);
    }

    /**
     * @param commentId
     * @param payload
     * @param currentUser only comment's owner can update it
     * @return details of updated comment
     */
    @Override
    public CommentDetails updateComment(Long commentId, NewCommentRequest payload, UserPrincipal currentUser) {
        return null;
    }

    /**
     * @param commentId
     * @param currentUser only comment's owner can delete it
     */
    @Override
    public void deleteComment(Long commentId, UserPrincipal currentUser) {

    }

    /**
     * This service only get the comments that have no parent.
     * If you want to fetch their replies, call to {@link #getCommentReplies(Long, Pageable)}
     *
     * @param subject   subject type, can be challenge or tutorial
     * @param subjectId if of challenge or tutorial
     * @param pageable  only contain size and page parameters, default fetching with size = 10 and page = 0
     * @return
     */
    @Override
    public PaginatedResponse<CommentSummary> getCommentList(CommentSubject subject, Long subjectId, Pageable pageable) {
        return null;
    }

    /**
     * @param commentId parent comment's id
     * @param pageable  only contain size and page parameters, default fetching with size = 10 and page = 0
     * @return
     */
    @Override
    public List<CommentSummary> getCommentReplies(Long commentId, Pageable pageable) {
        return null;
    }
}
