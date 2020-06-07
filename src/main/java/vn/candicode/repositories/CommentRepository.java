package vn.candicode.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import vn.candicode.models.ChallengeCommentEntity;
import vn.candicode.models.ChallengeEntity;

public interface CommentRepository extends JpaRepository<ChallengeCommentEntity, Long> {
    Long countAllByChallenge(ChallengeEntity challenge);
}
