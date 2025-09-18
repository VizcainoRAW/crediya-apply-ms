package co.com.crediya.consumer.dto;

public record TokenValidationData(
        boolean valid,
        UserDetailData user,
        Long expiresIn,
        String message
) {}