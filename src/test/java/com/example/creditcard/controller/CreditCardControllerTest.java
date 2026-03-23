package com.example.creditcard.controller;

import com.example.creditcard.model.CreditCard;
import com.example.creditcard.service.CreditCardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CreditCardController.class)
class CreditCardControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CreditCardService creditCardService;

    private CreditCard sampleCard() {
        return new CreditCard(1L, "Alice Johnson", "4111111111111111",
                LocalDate.of(2027, 6, 30), "VISA", 5000.0, 1200.0, true);
    }

    // -------------------------------------------------------------------------
    // GET /cards
    // -------------------------------------------------------------------------

    @Test
    void listCards_returns200() throws Exception {
        when(creditCardService.getAllCards()).thenReturn(List.of(sampleCard()));

        mockMvc.perform(get("/cards"))
                .andExpect(status().isOk())
                .andExpect(view().name("cards/list"))
                .andExpect(model().attributeExists("cards"));
    }

    // -------------------------------------------------------------------------
    // GET /cards/new
    // -------------------------------------------------------------------------

    @Test
    void showNewCardForm_returns200WithEmptyDto() throws Exception {
        mockMvc.perform(get("/cards/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("cards/form"))
                .andExpect(model().attributeExists("card", "cardTypes"));
    }

    // -------------------------------------------------------------------------
    // POST /cards/save
    // -------------------------------------------------------------------------

    @Test
    void saveCard_validData_redirectsToList() throws Exception {
        when(creditCardService.saveCard(any())).thenReturn(sampleCard());

        mockMvc.perform(post("/cards/save")
                        .param("cardholderName", "Alice Johnson")
                        .param("cardNumber", "4111111111111111")
                        .param("expiryDate", "2027-06-30")
                        .param("cardType", "VISA")
                        .param("creditLimit", "5000.0")
                        .param("currentBalance", "0.0")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards"));

        verify(creditCardService).saveCard(any());
    }

    @Test
    void saveCard_blankRequiredFields_returnsForm() throws Exception {
        mockMvc.perform(post("/cards/save")
                        .param("cardholderName", "")
                        .param("cardNumber", "")
                        .param("cardType", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("cards/form"))
                .andExpect(model().attributeHasFieldErrors("card",
                        "cardholderName", "cardNumber", "cardType"));
    }

    // -------------------------------------------------------------------------
    // GET /cards/edit/{id}
    // -------------------------------------------------------------------------

    @Test
    void showEditForm_existingCard_returns200() throws Exception {
        when(creditCardService.getCardById(1L)).thenReturn(Optional.of(sampleCard()));

        mockMvc.perform(get("/cards/edit/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("cards/form"))
                .andExpect(model().attributeExists("card", "cardTypes"));
    }

    @Test
    void showEditForm_missingCard_redirectsToList() throws Exception {
        when(creditCardService.getCardById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/cards/edit/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards"));
    }

    // -------------------------------------------------------------------------
    // POST /cards/update/{id}
    // -------------------------------------------------------------------------

    @Test
    void updateCard_validData_redirectsToList() throws Exception {
        when(creditCardService.updateCard(anyLong(), any())).thenReturn(sampleCard());

        mockMvc.perform(post("/cards/update/1")
                        .param("cardholderName", "Alice Johnson")
                        .param("cardNumber", "4111111111111111")
                        .param("expiryDate", "2027-06-30")
                        .param("cardType", "VISA")
                        .param("creditLimit", "5000.0")
                        .param("currentBalance", "1200.0")
                        .param("isActive", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards"));

        verify(creditCardService).updateCard(anyLong(), any());
    }

    @Test
    void updateCard_blankRequiredFields_returnsForm() throws Exception {
        mockMvc.perform(post("/cards/update/1")
                        .param("cardholderName", "")
                        .param("cardType", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("cards/form"))
                .andExpect(model().attributeHasFieldErrors("card",
                        "cardholderName", "cardType"));
    }

    // -------------------------------------------------------------------------
    // GET /cards/{id}
    // -------------------------------------------------------------------------

    @Test
    void viewCard_existingCard_returns200WithComputedAttributes() throws Exception {
        CreditCard card = sampleCard();
        when(creditCardService.getCardById(1L)).thenReturn(Optional.of(card));
        when(creditCardService.calculateAvailableCredit(card)).thenReturn(3800.0);
        when(creditCardService.calculateUtilizationPercentage(card)).thenReturn(24.0);

        mockMvc.perform(get("/cards/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("cards/detail"))
                .andExpect(model().attributeExists("card", "availableCredit", "utilization"));
    }

    @Test
    void viewCard_missingCard_redirectsToList() throws Exception {
        when(creditCardService.getCardById(99L)).thenReturn(Optional.empty());

        mockMvc.perform(get("/cards/99"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards"));
    }

    // -------------------------------------------------------------------------
    // POST /cards/delete/{id}
    // -------------------------------------------------------------------------

    @Test
    void deleteCard_redirectsToList() throws Exception {
        mockMvc.perform(post("/cards/delete/1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards"));

        verify(creditCardService).deleteCard(1L);
    }

    // -------------------------------------------------------------------------
    // POST /cards/payment/{id}
    // -------------------------------------------------------------------------

    @Test
    void makePayment_success_redirectsToDetail() throws Exception {
        when(creditCardService.makePayment(1L, 200.0)).thenReturn(sampleCard());

        mockMvc.perform(post("/cards/payment/1").param("amount", "200.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards/1"));
    }

    @Test
    void makePayment_exceedsBalance_redirectsToDetailWithError() throws Exception {
        when(creditCardService.makePayment(1L, 9999.0))
                .thenThrow(new IllegalArgumentException("Payment amount exceeds current balance."));

        mockMvc.perform(post("/cards/payment/1").param("amount", "9999.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards/1"));
    }

    // -------------------------------------------------------------------------
    // POST /cards/charge/{id}
    // -------------------------------------------------------------------------

    @Test
    void makeCharge_success_redirectsToDetail() throws Exception {
        when(creditCardService.makeCharge(1L, 500.0)).thenReturn(sampleCard());

        mockMvc.perform(post("/cards/charge/1").param("amount", "500.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards/1"));
    }

    @Test
    void makeCharge_exceedsLimit_redirectsToDetailWithError() throws Exception {
        when(creditCardService.makeCharge(1L, 99999.0))
                .thenThrow(new IllegalArgumentException("Charge exceeds credit limit."));

        mockMvc.perform(post("/cards/charge/1").param("amount", "99999.0"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/cards/1"));
    }

    // -------------------------------------------------------------------------
    // GET /cards/search
    // -------------------------------------------------------------------------

    @Test
    void searchCards_byName_returns200() throws Exception {
        when(creditCardService.search("Alice", null)).thenReturn(List.of(sampleCard()));

        mockMvc.perform(get("/cards/search").param("name", "Alice"))
                .andExpect(status().isOk())
                .andExpect(view().name("cards/list"))
                .andExpect(model().attributeExists("cards", "cardTypes"));
    }

    @Test
    void searchCards_byType_returns200() throws Exception {
        when(creditCardService.search(null, "VISA")).thenReturn(List.of(sampleCard()));

        mockMvc.perform(get("/cards/search").param("type", "VISA"))
                .andExpect(status().isOk())
                .andExpect(view().name("cards/list"))
                .andExpect(model().attributeExists("cards"));
    }

    @Test
    void searchCards_noParams_returnsAllCards() throws Exception {
        when(creditCardService.search(null, null)).thenReturn(List.of(sampleCard()));

        mockMvc.perform(get("/cards/search"))
                .andExpect(status().isOk())
                .andExpect(view().name("cards/list"));
    }
}
