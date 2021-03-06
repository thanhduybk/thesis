package vn.candicode.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
public class NewTestcaseListRequest extends Request {
    @NotNull(message = "Field 'testcases' is required but not be given")
    @Size(min = 1, message = "Must contain at least one testcase")
    @Valid
    private List<TestcaseRequest> testcases;
}
