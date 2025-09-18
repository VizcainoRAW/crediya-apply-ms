package co.com.crediya.consumer.dto;

public record TokenValidationResponse(
        boolean success,
        String message,
        TokenValidationData data,
        Object errors
) {}
