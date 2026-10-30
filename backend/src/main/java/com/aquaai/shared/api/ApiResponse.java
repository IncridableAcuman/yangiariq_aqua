package com.aquaai.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.time.Instant;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private final boolean success;
    private final T data;
    private final ApiErrorResponse error;
    @Builder.Default
    private final Instant timestamp = Instant.now();
    private final String correlationId;

    public static <T> ApiResponse<T> ok(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> ApiResponse<T> ok(T data, String correlationId) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .correlationId(correlationId)
                .build();
    }

    public static <T> ApiResponse<T> error(ApiErrorResponse error, String correlationId) {
        return ApiResponse.<T>builder()
                .success(false)
                .error(error)
                .correlationId(correlationId)
                .build();
    }
}
