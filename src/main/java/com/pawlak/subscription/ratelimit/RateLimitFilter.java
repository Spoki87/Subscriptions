package com.pawlak.subscription.ratelimit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class RateLimitFilter extends OncePerRequestFilter {

    private final RateLimitService rateLimitService;

    private static final Map<String, RateLimitEndpoint> PROTECTED_PATHS = Map.of(
            "/api/auth/login", RateLimitEndpoint.LOGIN,
            "/api/user/register", RateLimitEndpoint.REGISTER
    );

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String path = request.getRequestURI();
        Optional<RateLimitEndpoint> endpoint = resolveEndpoint(path);

        if (endpoint.isPresent()) {
            String ip = extractIp(request);
            boolean allowed = rateLimitService.isAllowed(ip, endpoint.get());

            if (!allowed) {
                writeRateLimitResponse(response);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private Optional<RateLimitEndpoint> resolveEndpoint(String path) {
        return PROTECTED_PATHS.entrySet().stream()
                .filter(entry -> path.equals(entry.getKey()))
                .map(Map.Entry::getValue)
                .findFirst();
    }

    private String extractIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        if (forwarded != null && !forwarded.isBlank()) {
            return forwarded.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void writeRateLimitResponse(HttpServletResponse response) throws IOException {
        response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {
                  "message": "Too many requests. Please try again later.",
                  "timestamp": "%s"
                }
                """.formatted(java.time.Instant.now()));
    }

}
