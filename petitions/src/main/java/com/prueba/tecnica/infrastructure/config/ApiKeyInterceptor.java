package com.prueba.tecnica.infrastructure.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Web Interceptor that validates the X-API-KEY header for protected routes.
 */
@Slf4j
@Component
public class ApiKeyInterceptor implements HandlerInterceptor {

    private static final String API_KEY_HEADER = "X-API-KEY";

    @Value("${app.security.api-key}")
    private String expectedApiKey;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String providedKey = request.getHeader(API_KEY_HEADER);

        if (!StringUtils.hasText(providedKey) || !providedKey.equals(expectedApiKey)) {
            log.warn("Unauthorized API access attempt to URI: {}. Missing or invalid API Key.",
                    request.getRequestURI());

            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.setContentType("application/json");
            response.getWriter()
                    .write("{\"success\":false,\"status\":401,\"message\":\"Invalid or missing X-API-KEY\"}");

            return false;
        }

        return true;
    }
}
