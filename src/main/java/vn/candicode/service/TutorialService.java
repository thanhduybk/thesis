package vn.candicode.service;

import vn.candicode.payload.request.NewTutorialRequest;
import vn.candicode.payload.request.TutorialPaginatedRequest;
import vn.candicode.payload.request.UpdateTutorialRequest;
import vn.candicode.payload.response.PaginatedResponse;
import vn.candicode.payload.response.TutorialDetails;
import vn.candicode.payload.response.TutorialSummary;
import vn.candicode.security.UserPrincipal;

public interface TutorialService {
    /**
     * @param payload
     * @param me
     * @return id of new tutorial
     */
    Long createTutorial(NewTutorialRequest payload, UserPrincipal me);

    /**
     * @param criteria
     * @return paginated list of tutorials
     */
    PaginatedResponse<TutorialSummary> getTutorialList(TutorialPaginatedRequest criteria);

    /**
     * @param criteria
     * @param myId
     * @return paginated list of my tutorials
     */
    PaginatedResponse<TutorialSummary> getMyTutorialList(TutorialPaginatedRequest criteria, Long myId);

    /**
     * @param tutorialId
     * @param me
     * @return details of tutorial with given id
     */
    TutorialDetails getTutorialDetails(Long tutorialId, UserPrincipal me);

    /**
     * @param tutorialId
     * @param payload
     * @param me
     */
    void updateTutorial(Long tutorialId, UpdateTutorialRequest payload, UserPrincipal me);

    /**
     * @param tutorialId
     * @param me
     */
    void removeTutorial(Long tutorialId, UserPrincipal me);
}
