package co.com.crediya.consumer.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record UserDetailData(
        String id,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String address,
        String phone,
        String email,
        BigDecimal baseSalary,
        String role,
        String documentType,
        String documentId
) {}
