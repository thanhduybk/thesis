package vn.candicode.payload.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class TestcaseRequest extends Request {
    /**
     * Only use this field when updating testcase
     */
    private Long testcaseId;

    @NotBlank(message = "Field 'input' is required but not be given")
    private String input;

    @NotBlank(message = "Field 'output' is required but not be given")
    private String output;

    private Boolean hidden;

    private Long timeout;

    public Boolean getHidden() {
        return hidden == null ? false : hidden;
    }

    public void setHidden(Boolean hidden) {
        this.hidden = hidden == null ? false : hidden;
    }

    public Long getTimeout() {
        return timeout == null ? 0 : timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout == null ? 0L : timeout;
    }

}
