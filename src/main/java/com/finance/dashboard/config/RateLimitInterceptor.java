package com.finance.dashboard.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitInterceptor implements HandlerInterceptor {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();

    @Override
    public boolean preHandle(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response, @NonNull Object handler) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String rateLimitKey;

        if (auth != null && auth.isAuthenticated() && !Objects.equals(auth.getPrincipal(), "anonymousUser")) {
            rateLimitKey = auth.getName();
        } else {
            rateLimitKey = request.getRemoteAddr();
        }

        Bucket bucket = cache.computeIfAbsent(rateLimitKey, this::createNewBucket);

        if (bucket.tryConsume(1)) {
            return true;
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("""
                    {
                        "status": 429,
                        "error": "Too Many Requests",
                        "message": "You have exhausted your API request quota. Please try again later."
                    }
                    """);
            return false;
        }
    }

    private Bucket createNewBucket(String key) {
        Bandwidth limit = Bandwidth.builder()
                .capacity(50)
                .refillIntervally(50, Duration.ofMinutes(1))
                .build();

        return Bucket.builder().addLimit(limit).build();
    }
}