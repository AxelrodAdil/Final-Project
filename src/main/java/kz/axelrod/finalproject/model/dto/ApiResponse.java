package kz.axelrod.finalproject.model.dto;

import lombok.Getter;

@Getter
public class ApiResponse<T> {

    private boolean success;

    private String error;

    private T result;

    public ApiResponse() {
    }

    public ApiResponse(boolean success, String error, T result) {
        this.success = success;
        this.error = error;
        this.result = result;
    }

    public static <T> ApiResponse<T> success(T result) {
        return new ApiResponse<>(true, null, result);
    }

    public static <T> ApiResponse<T> fail(String error) {
        return new ApiResponse<>(false, error, null);
    }
}

