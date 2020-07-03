package vn.candicode.payload.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateUserProfileRequest extends Request {
    private String firstName;
    private String lastName;
}
