package com.example.creditcard.controller;

import com.example.creditcard.model.CreditCard;
import com.example.creditcard.service.CreditCardService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/cards")
public class CreditCardController {

    @Autowired
    private CreditCardService creditCardService;

    // --- List all cards ---
    @GetMapping
    public String listCards(Model model) {
        List<CreditCard> cards = creditCardService.getAllCards();
        model.addAttribute("cards", cards);
        return "cards/list";
    }

    // --- Show form for new card ---
    @GetMapping("/new")
    public String showNewCardForm(Model model) {
        model.addAttribute("card", new CreditCard());
        model.addAttribute("cardTypes", List.of("VISA", "MASTERCARD", "AMEX", "DISCOVER"));
        return "cards/form";
    }

    // --- Save new card ---
    // BUG #6: Method is mapped to GET instead of POST — form submission will fail (405 Method Not Allowed).
    //         Fix: Change @GetMapping("/save") to @PostMapping("/save")
    @GetMapping("/save")
    public String saveCard(@Valid @ModelAttribute("card") CreditCard card,
                           BindingResult result,
                           Model model,
                           RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("cardTypes", List.of("VISA", "MASTERCARD", "AMEX", "DISCOVER"));
            return "cards/form";
        }
        creditCardService.saveCard(card);
        redirectAttributes.addFlashAttribute("successMessage", "Card saved successfully!");
        return "redirect:/cards";
    }

    // --- Show edit form ---
    @GetMapping("/edit/{id}")
    public String showEditForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return creditCardService.getCardById(id)
                .map(card -> {
                    model.addAttribute("card", card);
                    model.addAttribute("cardTypes", List.of("VISA", "MASTERCARD", "AMEX", "DISCOVER"));
                    return "cards/form";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Card not found.");
                    return "redirect:/cards";
                });
    }

    // --- Update card ---
    @PostMapping("/update/{id}")
    public String updateCard(@PathVariable Long id,
                             @Valid @ModelAttribute("card") CreditCard card,
                             BindingResult result,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("cardTypes", List.of("VISA", "MASTERCARD", "AMEX", "DISCOVER"));
            return "cards/form";
        }
        creditCardService.updateCard(id, card);
        redirectAttributes.addFlashAttribute("successMessage", "Card updated successfully!");
        return "redirect:/cards";
    }

    // --- View card detail ---
    @GetMapping("/{id}")
    public String viewCard(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        return creditCardService.getCardById(id)
                .map(card -> {
                    model.addAttribute("card", card);
                    double available = creditCardService.calculateAvailableCredit(id);
                    model.addAttribute("availableCredit", available);
                    double utilization = (card.getCurrentBalance() / card.getCreditLimit()) * 100;
                    model.addAttribute("utilization", String.format("%.1f", utilization));
                    return "cards/detail";
                })
                .orElseGet(() -> {
                    redirectAttributes.addFlashAttribute("errorMessage", "Card not found.");
                    return "redirect:/cards";
                });
    }

    // --- Delete card ---
    @PostMapping("/delete/{id}")
    public String deleteCard(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        creditCardService.deleteCard(id);
        redirectAttributes.addFlashAttribute("successMessage", "Card deleted successfully!");
        return "redirect:/cards";
    }

    // --- Make a payment ---
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

    // --- Make a charge ---
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

    // --- Search ---
    @GetMapping("/search")
    public String searchCards(@RequestParam(required = false) String name,
                              @RequestParam(required = false) String type,
                              Model model) {
        List<CreditCard> results;
        if (name != null && !name.isBlank()) {
            results = creditCardService.searchByHolder(name);
        } else if (type != null && !type.isBlank()) {
            results = creditCardService.getCardsByType(type);
        } else {
            results = creditCardService.getAllCards();
        }
        model.addAttribute("cards", results);
        model.addAttribute("searchName", name);
        model.addAttribute("searchType", type);
        model.addAttribute("cardTypes", List.of("VISA", "MASTERCARD", "AMEX", "DISCOVER"));
        return "cards/list";
    }
}
