package vn.candicode.exception.handler;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import org.springframework.boot.web.servlet.error.DefaultErrorAttributes;
import org.springframework.web.context.request.WebRequest;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Getter
@Builder
public class CandicodeError implements Serializable {
    private final int code;
    private final String message;
    private final String reason;
    private final String exception;
    private final String path;
    private final List<Error> errors;

    @Getter
    @AllArgsConstructor
    public static final class Details {
        private final String message;
        private final String reason;
    }

    public static final class Attributes extends DefaultErrorAttributes {
        private static final boolean INCLUDE_EXCEPTION = true;
        private static final boolean INCLUDE_STACKTRACE = false;

        public Attributes() {
            super(INCLUDE_EXCEPTION);
        }

        @Override
        public Map<String, Object> getErrorAttributes(WebRequest webRequest, boolean includeStackTrace) {
            Map<String, Object> defaultAttrs = super.getErrorAttributes(webRequest, INCLUDE_STACKTRACE);

            return Map.of(
                "code", defaultAttrs.getOrDefault("status", 500),
                "message", defaultAttrs.getOrDefault("error", "Internal server error"),
                "reason", defaultAttrs.getOrDefault("message", "Reason unknown"),
                "exception", defaultAttrs.getOrDefault("exception", "Exception unknown"),
                "path", defaultAttrs.getOrDefault("path", "Path unknown")
            );
        }
    }
}
