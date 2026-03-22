package com.example.creditcard.service;

import com.example.creditcard.model.CreditCard;
import com.example.creditcard.repository.CreditCardRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CreditCardService {

    // BUG #4: Field injection is used but the repository is never actually injected
    //         because the @Autowired annotation is missing.
    //         Fix: Add @Autowired above "private CreditCardRepository creditCardRepository;"
    private CreditCardRepository creditCardRepository;

    public List<CreditCard> getAllCards() {
        return creditCardRepository.findAll();
    }

    public List<CreditCard> getActiveCards() {
        return creditCardRepository.findByIsActiveTrue();
    }

    public Optional<CreditCard> getCardById(Long id) {
        return creditCardRepository.findById(id);
    }

    public Optional<CreditCard> getCardByNumber(String cardNumber) {
        return creditCardRepository.findByCardNumber(cardNumber);
    }

    public List<CreditCard> searchByHolder(String name) {
        return creditCardRepository.findByCardholderNameContainingIgnoreCase(name);
    }

    public List<CreditCard> getCardsByType(String cardType) {
        return creditCardRepository.findByCardType(cardType);
    }

    public List<CreditCard> getHighUtilizationCards(double percentage) {
        return creditCardRepository.findCardsWithHighUtilization(percentage);
    }

    @Transactional
    public CreditCard saveCard(CreditCard card) {
        return creditCardRepository.save(card);
    }

    @Transactional
    public CreditCard updateCard(Long id, CreditCard updatedCard) {
        CreditCard existing = creditCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credit card not found with id: " + id));

        existing.setCardholderName(updatedCard.getCardholderName());
        existing.setCardType(updatedCard.getCardType());
        existing.setCreditLimit(updatedCard.getCreditLimit());
        existing.setCurrentBalance(updatedCard.getCurrentBalance());
        existing.setExpiryDate(updatedCard.getExpiryDate());
        existing.setIsActive(updatedCard.getIsActive());

        return creditCardRepository.save(existing);
    }

    @Transactional
    public void deleteCard(Long id) {
        creditCardRepository.deleteById(id);
    }

    @Transactional
    public CreditCard makePayment(Long id, Double amount) {
        CreditCard card = creditCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credit card not found with id: " + id));

        // BUG #5: Logic error — payment increases balance instead of decreasing it.
        //         Fix: Change "+" to "-"
        double newBalance = card.getCurrentBalance() + amount;
        if (newBalance < 0) {
            throw new IllegalArgumentException("Payment amount exceeds current balance.");
        }
        card.setCurrentBalance(newBalance);
        return creditCardRepository.save(card);
    }

    @Transactional
    public CreditCard makeCharge(Long id, Double amount) {
        CreditCard card = creditCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credit card not found with id: " + id));

        double newBalance = card.getCurrentBalance() + amount;
        if (newBalance > card.getCreditLimit()) {
            throw new IllegalArgumentException("Charge exceeds credit limit.");
        }
        card.setCurrentBalance(newBalance);
        return creditCardRepository.save(card);
    }

    public double calculateAvailableCredit(Long id) {
        CreditCard card = creditCardRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Credit card not found with id: " + id));
        return card.getCreditLimit() - card.getCurrentBalance();
    }
}
