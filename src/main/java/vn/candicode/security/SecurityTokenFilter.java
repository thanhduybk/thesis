package vn.candicode.security;

import io.jsonwebtoken.ExpiredJwtException;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import vn.candicode.exception.TokenExpiredException;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.regex.Pattern;

@Component
@Log4j2
public class SecurityTokenFilter extends OncePerRequestFilter {
    private static final Pattern NUMERIC = Pattern.compile("\\d+");

    private final SecurityTokenProvider tokenProvider;
    private final UserPrincipalService userPrincipalService;

    private final List<String> GET_URI_WHITELIST = List.of("/api/tags", "/api/categories", "/api/challenges", "/api/tutorials", "/api/contests", "/api/submissions", "/api/tags", "/api/categories");

    public SecurityTokenFilter(SecurityTokenProvider tokenProvider, UserPrincipalService userPrincipalService) {
        this.tokenProvider = tokenProvider;
        this.userPrincipalService = userPrincipalService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, FilterChain filterChain) throws ServletException, IOException {
        final String token = tokenProvider.getToken(httpServletRequest);

        try {
            if (StringUtils.hasText(token) && tokenProvider.validated(token)) {
                String tokenSubject = tokenProvider.getSubject(token);

                UserDetails details = userPrincipalService.loadUserByEmail(tokenSubject);

                UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(details, null, details.getAuthorities());

                auth.setDetails(new WebAuthenticationDetailsSource().buildDetails(httpServletRequest));

                SecurityContextHolder.getContext().setAuthentication(auth);
            }
        } catch (Exception e) {
            log.warn("Could not set up security authentication. Exception - {}", e.getClass().getSimpleName());
        }

        filterChain.doFilter(httpServletRequest, httpServletResponse);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        if ("GET".equals(request.getMethod())) {
            if (GET_URI_WHITELIST.contains(request.getRequestURI())) {
                return true;
            } else if (request.getRequestURI().contains("comment")) {
                return true;
            } else if (request.getRequestURI().contains("me")) {
                return false;
            } else return isNumeric(request.getRequestURI().substring(request.getRequestURI().lastIndexOf("/") + 1));
        } else if ("POST".equals(request.getMethod()) && request.getRequestURI().equals("/api/students")) {
            return true;
        }

        return false;
    }

    private boolean isNumeric(String s) {
        if (s == null) {
            return false;
        }

        return NUMERIC.matcher(s).matches();
    }
}
