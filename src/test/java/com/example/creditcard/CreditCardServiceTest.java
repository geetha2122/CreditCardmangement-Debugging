package com.example.creditcard;

import com.example.creditcard.model.CreditCard;
import com.example.creditcard.repository.CreditCardRepository;
import com.example.creditcard.service.CreditCardService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CreditCardServiceTest {

    @Mock
    private CreditCardRepository creditCardRepository;

    @InjectMocks
    private CreditCardService creditCardService;

    private CreditCard sampleCard;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        sampleCard = new CreditCard(1L, "Alice Johnson", "4111111111111111",
                LocalDate.of(2027, 6, 30), "VISA", 5000.0, 1200.0, true);
    }

    @Test
    void testGetCardById_found() {
        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(sampleCard));
        Optional<CreditCard> result = creditCardService.getCardById(1L);
        assertTrue(result.isPresent());
        assertEquals("Alice Johnson", result.get().getCardholderName());
    }

    @Test
    void testGetCardById_notFound() {
        when(creditCardRepository.findById(99L)).thenReturn(Optional.empty());
        Optional<CreditCard> result = creditCardService.getCardById(99L);
        assertFalse(result.isPresent());
    }

    @Test
    void testMakeCharge_withinLimit() {
        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(sampleCard));
        when(creditCardRepository.save(any(CreditCard.class))).thenAnswer(i -> i.getArguments()[0]);

        CreditCard result = creditCardService.makeCharge(1L, 500.0);
        assertEquals(1700.0, result.getCurrentBalance(), 0.01);
    }

    @Test
    void testMakeCharge_exceedsLimit() {
        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(sampleCard));
        assertThrows(IllegalArgumentException.class, () -> creditCardService.makeCharge(1L, 5000.0));
    }

    // BUG #9: Test asserts wrong expected value for payment.
    //         After a payment of $200 on a balance of $1200, balance should be $1000 (not $1400).
    //         But this test also reveals BUG #5 in the service — the payment logic adds instead of subtracts.
    //         Fix the service first (BUG #5), then fix the assertion: assertEquals(1000.0, ...)
    @Test
    void testMakePayment_reducesBalance() {
        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(sampleCard));
        when(creditCardRepository.save(any(CreditCard.class))).thenAnswer(i -> i.getArguments()[0]);

        CreditCard result = creditCardService.makePayment(1L, 200.0);
        // This assertion is WRONG — it expects the buggy behavior (balance increased).
        // After fixing BUG #5, change the expected value to 1000.0
        assertEquals(1400.0, result.getCurrentBalance(), 0.01);
    }

    @Test
    void testCalculateAvailableCredit() {
        when(creditCardRepository.findById(1L)).thenReturn(Optional.of(sampleCard));
        double available = creditCardService.calculateAvailableCredit(1L);
        assertEquals(3800.0, available, 0.01);
    }
}
