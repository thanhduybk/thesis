package vn.candicode.payloads.requests;

import lombok.Getter;
import lombok.Setter;
import vn.candicode.payloads.GenericRequest;
import vn.candicode.payloads.services.impl.UniqueUserValidator;
import vn.candicode.payloads.validators.ConfirmPassword;
import vn.candicode.payloads.validators.Email;
import vn.candicode.payloads.validators.Unique;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@ConfirmPassword
public class StudentRequest extends GenericRequest implements HasPassword {
    @NotBlank(message = "Field 'email' is required but not be given")
    @Email
    @Unique(message = "Email has been already in use", service = UniqueUserValidator.class, column = "email")
    private String email;

    @NotBlank(message = "Field 'password' is required but not be given")
    @Size(min = 6, message = "Password should contain at least 6 characters")
    private String password;

    @NotBlank(message = "Field 'confirmPassword' is required but not be given")
    private String confirmPassword;

    @NotBlank(message = "Field 'firstName' is required but not be given")
    private String firstName;

    @NotBlank(message = "Field 'lastName' is required but not be given")
    private String lastName;
}
