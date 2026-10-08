package org.vti.jamie.com.project_spring_boot.dto.response;

import java.time.Instant;
import java.util.Map;

public record ApiResponse<T>(
        boolean success,
        int status,
        String code,
        String message,
        T data,
        Map<String, String> errors,
        Instant timestamp
) {
    public static <T> ApiResponse<T> success(int status, String message, T data) {
        return new ApiResponse<>(true, status, "SUCCESS", message, data, Map.of(), Instant.now());
    }

    public static ApiResponse<Void> error(int status, String code, String message,
                                           Map<String, String> errors) {
        return new ApiResponse<>(false, status, code, message, null, Map.copyOf(errors), Instant.now());
    }
}
