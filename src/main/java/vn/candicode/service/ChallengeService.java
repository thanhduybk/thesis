package vn.candicode.service;

import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;
import vn.candicode.payload.request.NewChallengeRequest;
import vn.candicode.payload.request.UpdateChallengeRequest;
import vn.candicode.payload.response.ChallengeDetails;
import vn.candicode.payload.response.ChallengeSummary;
import vn.candicode.payload.response.DirectoryTree;
import vn.candicode.payload.response.PaginatedResponse;
import vn.candicode.security.UserPrincipal;

public interface ChallengeService {
    /**
     * @param payload
     * @param author
     * @return id of new challenge
     */
    Long createChallenge(NewChallengeRequest payload, UserPrincipal author);

    /**
     * @param file   must be a zip file
     * @param author
     * @return
     */
    DirectoryTree storeChallengeSource(MultipartFile file, UserPrincipal author);

    /**
     * @param pageable
     * @return paginated list of challenges
     */
    PaginatedResponse<ChallengeSummary> getChallengeList(Pageable pageable);

    /**
     * @param pageable
     * @param myId
     * @param wantContestChallenge should load only contest challenges ?
     * @return paginated list of my challenges
     */
    PaginatedResponse<ChallengeSummary> getMyChallengeList(Pageable pageable, Long myId, Boolean wantContestChallenge);

    /**
     * @param challengeId
     * @param me          For determining if should render testcase output or not
     * @return details of challenge with given id
     */
    ChallengeDetails getChallengeDetails(Long challengeId, UserPrincipal me);

    /**
     * Only author can edit challenge
     *
     * @param challengeId
     * @param payload
     * @param currentUser
     */
    void updateChallenge(Long challengeId, UpdateChallengeRequest payload, UserPrincipal currentUser);

    /**
     * <ul>
     *     <li>Only author can delete his challenge</li>
     *     <li>Call this method will delete both DB records and related filesystem directories</li>
     * </ul>
     *
     * @param challengeId
     * @param currentUser
     */
    void deleteChallenge(Long challengeId, UserPrincipal currentUser);
}
