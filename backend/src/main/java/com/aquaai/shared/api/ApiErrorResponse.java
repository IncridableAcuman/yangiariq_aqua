package com.aquaai.shared.api;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiErrorResponse {

    private final String code;
    private final String message;
    private final List<String> details;

    public static ApiErrorResponse of(String code, String message) {
        return ApiErrorResponse.builder()
                .code(code)
                .message(message)
                .build();
    }

    public static ApiErrorResponse of(String code, String message, List<String> details) {
        return ApiErrorResponse.builder()
                .code(code)
                .message(message)
                .details(details)
                .build();
    }
}
