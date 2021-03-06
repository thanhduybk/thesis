package vn.candicode.payload.response;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ContestChallenge implements Serializable {
    private Long challengeId;
    private String title;
    private String slug;
}
