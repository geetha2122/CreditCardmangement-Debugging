package com.example.creditcard.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

// BUG #1: @Entity annotation is missing — JPA will not recognize this as a managed entity.
//         Fix: Add @Entity above @Table
@Table(name = "credit_cards")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreditCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Cardholder name is required")
    @Column(nullable = false)
    private String cardholderName;

    // BUG #2: Column length is set to 10, but a 16-digit card number + any format requires 19 chars.
    //         Fix: Change length = 10 to length = 19
    @NotBlank(message = "Card number is required")
    @Column(nullable = false, unique = true, length = 10)
    private String cardNumber;

    @NotNull(message = "Expiry date is required")
    @Future(message = "Expiry date must be in the future")
    @Column(nullable = false)
    private LocalDate expiryDate;

    @NotBlank(message = "Card type is required")
    @Column(nullable = false)
    private String cardType;   // VISA, MASTERCARD, AMEX, etc.

    @DecimalMin(value = "0.0", message = "Credit limit must be positive")
    @Column(nullable = false)
    private Double creditLimit;

    @DecimalMin(value = "0.0", message = "Balance cannot be negative")
    private Double currentBalance = 0.0;

    @Column(nullable = false)
    private Boolean isActive = true;
}
