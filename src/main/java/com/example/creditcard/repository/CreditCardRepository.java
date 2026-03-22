package com.example.creditcard.repository;

import com.example.creditcard.model.CreditCard;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CreditCardRepository extends JpaRepository<CreditCard, Long> {

    // Find by cardholder name (case-insensitive)
    List<CreditCard> findByCardholderNameContainingIgnoreCase(String name);

    // Find by card type
    List<CreditCard> findByCardType(String cardType);

    // Find all active cards
    List<CreditCard> findByIsActiveTrue();

    // BUG #3: JPQL query references wrong field name "cardNo" instead of "cardNumber".
    //         Fix: Change "c.cardNo" to "c.cardNumber"
    @Query("SELECT c FROM CreditCard c WHERE c.cardNo = :number")
    Optional<CreditCard> findByCardNumber(@Param("number") String cardNumber);

    // Find cards where balance exceeds a percentage of credit limit
    @Query("SELECT c FROM CreditCard c WHERE (c.currentBalance / c.creditLimit) * 100 >= :percentage")
    List<CreditCard> findCardsWithHighUtilization(@Param("percentage") double percentage);
}
