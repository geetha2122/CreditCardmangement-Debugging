package com.example.creditcard.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class CardFormDto {

    private Long id;

    @NotBlank(message = "Cardholder name is required")
    private String cardholderName;

    @NotBlank(message = "Card number is required")
    private String cardNumber;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    private LocalDate expiryDate;

    @NotBlank(message = "Card type is required")
    private String cardType;

    @NotNull(message = "Credit limit is required")
    @DecimalMin(value = "0.0", message = "Credit limit must be positive")
    private Double creditLimit;

    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    private Double currentBalance = 0.0;

    private Boolean isActive = true;
}
