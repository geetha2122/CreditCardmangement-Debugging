# CreditCardManagement — Debugging Exercise

## Bug List (9 Bugs to Find & Fix)

---

### BUG #1 — Missing `@Entity` on Model
- **File:** `src/main/java/com/example/creditcard/model/CreditCard.java`
- **Symptom:** App fails to start with `Not a managed type: class CreditCard`
- **Cause:** `@Entity` annotation is absent, so JPA doesn't recognize it as a DB entity.
- **Fix:** Add `@Entity` above `@Table(name = "credit_cards")`

---

### BUG #2 — Wrong Column Length for Card Number
- **File:** `src/main/java/com/example/creditcard/model/CreditCard.java`
- **Symptom:** Card numbers get silently truncated or a constraint violation occurs.
- **Cause:** `@Column(length = 10)` is too short for a 16-digit card number.
- **Fix:** Change `length = 10` to `length = 19`

---

### BUG #3 — Wrong Field Name in JPQL Query
- **File:** `src/main/java/com/example/creditcard/repository/CreditCardRepository.java`
- **Symptom:** `QueryException` / `could not resolve property: cardNo`
- **Cause:** JPQL query references `c.cardNo` but the entity field is `cardNumber`.
- **Fix:** Change `c.cardNo` to `c.cardNumber`

---

### BUG #4 — Missing `@Autowired` on Repository in Service
- **File:** `src/main/java/com/example/creditcard/service/CreditCardService.java`
- **Symptom:** `NullPointerException` on any service call at runtime.
- **Cause:** `CreditCardRepository` field lacks `@Autowired`, so it's never injected.
- **Fix:** Add `@Autowired` above `private CreditCardRepository creditCardRepository;`

---

### BUG #5 — Payment Increases Balance Instead of Decreasing It
- **File:** `src/main/java/com/example/creditcard/service/CreditCardService.java` — `makePayment()`
- **Symptom:** After a payment, the balance goes up instead of down.
- **Cause:** Logic uses `+` instead of `-`: `card.getCurrentBalance() + amount`
- **Fix:** Change `+` to `-`: `double newBalance = card.getCurrentBalance() - amount;`

---

### BUG #6 — Save Endpoint Mapped to GET Instead of POST
- **File:** `src/main/java/com/example/creditcard/controller/CreditCardController.java`
- **Symptom:** Submitting the "Add Card" form returns `405 Method Not Allowed`.
- **Cause:** `@GetMapping("/save")` should be `@PostMapping("/save")`.
- **Fix:** Change `@GetMapping("/save")` to `@PostMapping("/save")`

---

### BUG #7 — Thymeleaf Template Variable Name Mismatch
- **File:** `src/main/resources/templates/cards/list.html`
- **Symptom:** `TemplateProcessingException` or all columns show empty values.
- **Cause:** Loop declares `creditCard` in `th:each` but inner cells reference `card`.
- **Fix:** Change `th:each="creditCard : ${cards}"` to `th:each="card : ${cards}"`

---

### BUG #8 — Edit Form Always Posts to Create Endpoint
- **File:** `src/main/resources/templates/cards/form.html`
- **Symptom:** Editing a card creates a duplicate instead of updating the existing one.
- **Cause:** `th:action="@{/cards/save}"` is hardcoded regardless of whether this is a create or edit.
- **Fix:** Change to:
  ```html
  th:action="${card.id == null} ? @{/cards/save} : @{/cards/update/{id}(id=${card.id})}"
  ```

---

### BUG #9 — Unit Test Asserts Wrong Expected Value
- **File:** `src/test/java/com/example/creditcard/CreditCardServiceTest.java` — `testMakePayment_reducesBalance()`
- **Symptom:** Test passes only because BUG #5 makes payments add to balance.
- **Cause:** `assertEquals(1400.0, ...)` expects the broken behavior.
- **Fix:** First fix BUG #5, then update test: `assertEquals(1000.0, result.getCurrentBalance(), 0.01)`

---

## How to Run

```bash
# Build & run
mvn spring-boot:run

# Run tests
mvn test

# Access app
http://localhost:8080/cards

# H2 Console (JDBC URL: jdbc:h2:mem:creditcarddb)
http://localhost:8080/h2-console
```

## Debugging Order (Recommended)

1. **BUG #1** (app won't start) → **BUG #4** (NPE on launch) → **BUG #3** (query error)
2. **BUG #2** (data truncation, visible in H2 console)
3. **BUG #6** (form submit fails) → **BUG #8** (edit creates duplicate)
4. **BUG #7** (list page empty/crashes)
5. **BUG #5** + **BUG #9** (logic error caught by test)
