package com.example.creditcard.controller;

import com.example.creditcard.dto.CardFormDto;
import com.example.creditcard.model.CreditCard;
import com.example.creditcard.service.CreditCardService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cards")
public class CreditCardController {

    private static final List<String> CARD_TYPES = List.of("VISA", "MASTERCARD", "AMEX", "DISCOVER");

    private final CreditCardService creditCardService;

    public CreditCardController(CreditCardService creditCardService) {
        this.creditCardService = creditCardService;
    }

    @GetMapping
    public String listCards(Model model) {
        model.addAttribute("cards", creditCardService.getAllCards());
        return "cards/list";
    }

    @GetMapping("/new")
    public String showNewCardForm(Model model) {
        model.addAttribute("card", new CardFormDto());
        model.addAttribute("cardTypes", CARD_TYPES);
        return "cards/form";
    }

    @PostMapping("/save")
    public String saveCard(@Valid @ModelAttribute("card") CardFormDto dto,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("cardTypes", CARD_TYPES);
            return "cards/form";
        }
        creditCardService.saveCard(toEntity(dto));
        redirectAttributes.addFlashAttribute("successMessage", "Card saved successfully!");
        return "redirect:/cards";
    }

    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return creditCardService.getCardById(id)
                .map(card -> {
                    model.addAttribute("card", toDto(card));
                    model.addAttribute("cardTypes", CARD_TYPES);
                    return "cards/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Card not found.");
                    return "redirect:/cards";
                });
    }

    @PostMapping("/update/{id}")
    public String updateCard(@PathVariable Long id,
                             @Valid @ModelAttribute("card") CardFormDto dto,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("cardTypes", CARD_TYPES);
            return "cards/form";
        }
        creditCardService.updateCard(id, toEntity(dto));
        redirectAttributes.addFlashAttribute("successMessage", "Card updated successfully!");
        return "redirect:/cards";
    }

    @GetMapping("/{id}")
    public String viewCard(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return creditCardService.getCardById(id)
                .map(card -> {
                    model.addAttribute("card", card);
                    model.addAttribute("availableCredit", creditCardService.calculateAvailableCredit(card));
                    model.addAttribute("utilization", String.format("%.1f", creditCardService.calculateUtilizationPercentage(card)));
                    return "cards/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Card not found.");
                    return "redirect:/cards";
                });
    }

    @PostMapping("/delete/{id}")
    public String deleteCard(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        creditCardService.deleteCard(id);
        redirectAttributes.addFlashAttribute("successMessage", "Card deleted successfully!");
        return "redirect:/cards";
    }

    @PostMapping("/payment/{id}")
    public String makePayment(@PathVariable Long id,
                              @RequestParam Double amount,
                              RedirectAttributes redirectAttributes) {
        try {
            creditCardService.makePayment(id, amount);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Payment of $" + amount + " processed.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cards/" + id;
    }

    @PostMapping("/charge/{id}")
    public String makeCharge(@PathVariable Long id,
                             @RequestParam Double amount,
                             RedirectAttributes redirectAttributes) {
        try {
            creditCardService.makeCharge(id, amount);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Charge of $" + amount + " applied.");
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/cards/" + id;
    }

    @GetMapping("/search")
    public String searchCards(@RequestParam(required = false) String name,
                              @RequestParam(required = false) String type,
                              Model model) {
        model.addAttribute("cards", creditCardService.search(name, type));
        model.addAttribute("searchName", name);
        model.addAttribute("searchType", type);
        model.addAttribute("cardTypes", CARD_TYPES);
        return "cards/list";
    }

    // --- Mappers ---

    private CardFormDto toDto(CreditCard card) {
        CardFormDto dto = new CardFormDto();
        dto.setId(card.getId());
        dto.setCardholderName(card.getCardholderName());
        dto.setCardNumber(card.getCardNumber());
        dto.setExpiryDate(card.getExpiryDate());
        dto.setCardType(card.getCardType());
        dto.setCreditLimit(card.getCreditLimit());
        dto.setCurrentBalance(card.getCurrentBalance());
        dto.setIsActive(card.getIsActive());
        return dto;
    }

    private CreditCard toEntity(CardFormDto dto) {
        CreditCard card = new CreditCard();
        card.setId(dto.getId());
        card.setCardholderName(dto.getCardholderName());
        card.setCardNumber(dto.getCardNumber());
        card.setExpiryDate(dto.getExpiryDate());
        card.setCardType(dto.getCardType());
        card.setCreditLimit(dto.getCreditLimit());
        card.setCurrentBalance(dto.getCurrentBalance() != null ? dto.getCurrentBalance() : 0.0);
        card.setIsActive(dto.getIsActive() != null ? dto.getIsActive() : true);
        return card;
    }
}
