package vn.candicode.util;

import vn.candicode.entity.TutorialEntity;
import vn.candicode.payload.response.TutorialDetails;
import vn.candicode.payload.response.TutorialSummary;

import java.util.stream.Collectors;

public class TutorialBeanUtils {
    public static TutorialSummary summarize(TutorialEntity entity) {
        TutorialSummary summary = new TutorialSummary();

        summary.setTitle(entity.getTitle());
        summary.setAuthor(entity.getAuthor().getFullName());
        summary.setBanner(entity.getBanner());
        summary.setCreatedAt(entity.getCreatedAt().format(DatetimeUtils.JSON_DATETIME_FORMAT));
        summary.setDescription(entity.getBrieflyContent());
        summary.setDislikes(entity.getDislikes());
        summary.setLikes(entity.getLikes());
        summary.setTags(entity.getTags());
        summary.setTutorialId(entity.getTutorialId());
        summary.setCategories(entity.getCategories().stream().map(c -> c.getCategory().getName()).collect(Collectors.toSet()));
        summary.setNumComments(entity.getComments().size());

        return summary;
    }

    public static TutorialDetails details(TutorialEntity entity) {
        TutorialDetails details = new TutorialDetails();

        details.setTitle(entity.getTitle());
        details.setAuthor(entity.getAuthor().getFullName());
        details.setBanner(entity.getBanner());
        details.setCreatedAt(entity.getCreatedAt().format(DatetimeUtils.JSON_DATETIME_FORMAT));
        details.setDescription(entity.getBrieflyContent());
        details.setDislikes(entity.getDislikes());
        details.setLikes(entity.getLikes());
        details.setTags(entity.getTags());
        details.setTutorialId(entity.getTutorialId());
        details.setCategories(entity.getCategories().stream().map(c -> c.getCategory().getName()).collect(Collectors.toSet()));
        details.setNumComments(entity.getComments().size());
        details.setContent(entity.getContent());

        return details;
    }
}
