package co.com.crediya.consumer.dto;

public record TokenValidationRequest(
        String token,
        String service
) {}
