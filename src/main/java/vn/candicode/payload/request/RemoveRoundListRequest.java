package vn.candicode.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Getter
@Setter
public class RemoveRoundListRequest extends Request {
    @NotNull(message = "Field 'roundIds' is required but not be given")
    @Size(min = 1, message = "Must contain at least 1 testcase")
    private List<Long> roundIds;
}
