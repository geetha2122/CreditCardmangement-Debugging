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

    List<CreditCard> findByCardholderNameContainingIgnoreCase(String name);

    List<CreditCard> findByCardType(String cardType);

    List<CreditCard> findByIsActiveTrue();

    @Query("SELECT c FROM CreditCard c WHERE c.cardNumber = :number")
    Optional<CreditCard> findByCardNumber(@Param("number") String cardNumber);

    // Find cards where balance exceeds a percentage of credit limit
    @Query("SELECT c FROM CreditCard c WHERE (c.currentBalance / c.creditLimit) * 100 >= :percentage")
    List<CreditCard> findCardsWithHighUtilization(@Param("percentage") double percentage);
}
