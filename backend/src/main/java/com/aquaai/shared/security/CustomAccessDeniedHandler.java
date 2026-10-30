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
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomAccessDeniedHandler implements AccessDeniedHandler {

    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request,
                       HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        String correlationId = CorrelationIdFilter.getCurrentCorrelationId();
        log.warn("Access denied for URI: {} [correlationId={}]", request.getRequestURI(), correlationId);

        response.setStatus(HttpServletResponse.SC_FORBIDDEN);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setHeader(CorrelationIdFilter.CORRELATION_ID_HEADER, correlationId);

        ApiErrorResponse error = ApiErrorResponse.of("ACCESS_DENIED", "You do not have permission to access this resource");
        ApiResponse<Void> apiResponse = ApiResponse.error(error, correlationId);

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
    }
}
