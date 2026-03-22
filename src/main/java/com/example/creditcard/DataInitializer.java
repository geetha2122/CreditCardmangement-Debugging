package com.example.creditcard;

import com.example.creditcard.model.CreditCard;
import com.example.creditcard.repository.CreditCardRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDate;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner initData(CreditCardRepository repo) {
        return args -> {
            repo.save(new CreditCard(null, "Alice Johnson", "4111111111111111",
                    LocalDate.of(2027, 6, 30), "VISA", 5000.0, 1200.50, true));

            repo.save(new CreditCard(null, "Bob Smith", "5500005555555559",
                    LocalDate.of(2026, 12, 31), "MASTERCARD", 8000.0, 3450.00, true));

            repo.save(new CreditCard(null, "Carol White", "378282246310005",
                    LocalDate.of(2028, 3, 31), "AMEX", 15000.0, 0.0, true));

            repo.save(new CreditCard(null, "David Brown", "6011111111111117",
                    LocalDate.of(2025, 9, 30), "DISCOVER", 3000.0, 2900.00, true));

            repo.save(new CreditCard(null, "Eve Davis", "4012888888881881",
                    LocalDate.of(2027, 1, 31), "VISA", 10000.0, 500.0, false));
        };
    }
}
