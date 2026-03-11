package com.prueba.tecnica.infrastructure.rest.common;

public record ApiResponse<T>(
        boolean success,
        int status,
        String message,
        T data,
        Object errors) {

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, 200, message, data, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return success(data, "Success");
    }

    public static <T> ApiResponse<T> success(T data, String message, int status) {
        return new ApiResponse<>(true, status, message, data, null);
    }

    public static <T> ApiResponse<T> error(int status, String message, Object errors) {
        return new ApiResponse<>(false, status, message, null, errors);
    }

    public static <T> ApiResponse<T> error(int status, String message) {
        return new ApiResponse<>(false, status, message, null, null);
    }
}
