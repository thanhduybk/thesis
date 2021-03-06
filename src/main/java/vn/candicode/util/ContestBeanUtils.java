package vn.candicode.util;

import com.github.slugify.Slugify;
import vn.candicode.entity.ChallengeEntity;
import vn.candicode.entity.ContestChallengeEntity;
import vn.candicode.entity.ContestEntity;
import vn.candicode.entity.ContestRoundEntity;
import vn.candicode.payload.response.ContestChallenge;
import vn.candicode.payload.response.ContestDetails;
import vn.candicode.payload.response.ContestRound;
import vn.candicode.payload.response.ContestSummary;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

public class ContestBeanUtils {
    private static final Slugify SLUGIFY = new Slugify();

    public static ContestSummary summarize(ContestEntity contest) {
        ContestSummary summary = new ContestSummary();

        summary.setContestId(contest.getContestId());
        summary.setBanner(contest.getBanner());
        summary.setDescription(contest.getDescription());
        summary.setRegistrationDeadline(contest.getRegistrationDeadline().format(DatetimeUtils.JSON_DATETIME_FORMAT));
        summary.setTags(contest.getTags());
        summary.setTitle(contest.getTitle());
        summary.setSlug(SLUGIFY.slugify(contest.getTitle()));
        summary.setStatus("ongoing");
        summary.setAuthor(contest.getAuthor().getFullName());
        summary.setAvailable(contest.getAvailable());
        summary.setMaxRegister(contest.getMaxRegister());

        return summary;
    }

    public static ContestDetails details(ContestEntity contest) {
        ContestDetails details = new ContestDetails();

        details.setAuthor(contest.getAuthor().getFullName());
        details.setContestId(contest.getContestId());
        details.setBanner(contest.getBanner());
        details.setDescription(contest.getDescription());
        details.setRegistrationDeadline(contest.getRegistrationDeadline().format(DatetimeUtils.JSON_DATETIME_FORMAT));
        details.setTags(contest.getTags());
        details.setTitle(contest.getTitle());
        details.setSlug(SLUGIFY.slugify(contest.getTitle()));
        details.setStatus("ongoing");
        details.setAvailable(contest.getAvailable());
        details.setMaxRegister(contest.getMaxRegister());
        details.setContent(contest.getContent());

        for (ContestRoundEntity contestRound : contest.getRounds()) {
            details.getRounds().add(details(contestRound));
        }

        return details;
    }

    private static ContestRound details(ContestRoundEntity round) {
        ContestRound details = new ContestRound();

        details.setRoundId(round.getContestRoundId());
        details.setName(round.getName());
        details.setStartsAt(round.getStartsAt().format(DatetimeUtils.JSON_DATETIME_FORMAT));
        details.setEndsAt(round.getStartsAt().plus(round.getDuration(), ChronoUnit.MINUTES).format(DatetimeUtils.JSON_DATETIME_FORMAT));

        for (ContestChallengeEntity contestChallengeEntity : round.getChallenges()) {
            details.getChallenges().add(details(contestChallengeEntity));
        }

        return details;
    }

    private static ContestChallenge details(ContestChallengeEntity entity) {
        ContestChallenge details = new ContestChallenge();

        ChallengeEntity challenge = entity.getChallenge();

        details.setChallengeId(challenge.getChallengeId());
        details.setTitle(challenge.getTitle());
        details.setSlug(SLUGIFY.slugify(challenge.getTitle()));

        return details;
    }

    private static String contestStatus(LocalDateTime regDeadline) {
        return null;
    }
}
