package com.aquaai.shared.security;

import com.aquaai.shared.api.ApiErrorResponse;
import com.aquaai.shared.api.ApiResponse;
import com.aquaai.shared.api.CorrelationIdFilter;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request,
                         HttpServletResponse response,
                         AuthenticationException authException) throws IOException {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        log.warn("Unauthorized access attempt on URI: {} [correlationId={}]", request.getRequestURI(), correlationId);

        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId);

        ApiErrorResponse error = ApiErrorResponse.of("AUTHENTICATION_REQUIRED", "Full authentication is required to access this resource");
        ApiResponse<Void> apiResponse = ApiResponse.error(error, correlationId);

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
