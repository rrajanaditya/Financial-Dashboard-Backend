package com.finance.dashboard.config;

import com.finance.dashboard.model.*;
import com.finance.dashboard.repository.FinancialRecordRepository;
import com.finance.dashboard.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Component
@RequiredArgsConstructor
public class DatabaseSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final FinancialRecordRepository recordRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        // Only run if the database is totally empty
        if (userRepository.count() == 0) {

            // 1. Seed Users (All use "password123")
            String password = passwordEncoder.encode("password123");

            User admin = User.builder().name("System Admin").email("admin@finance.com").password(password).role(Role.ADMIN).status(UserStatus.ACTIVE).build();
            User analyst = User.builder().name("Data Analyst").email("analyst@finance.com").password(password).role(Role.ANALYST).status(UserStatus.ACTIVE).build();
            User viewer = User.builder().name("Read Only Viewer").email("viewer@finance.com").password(password).role(Role.VIEWER).status(UserStatus.ACTIVE).build();

            userRepository.saveAll(List.of(admin, analyst, viewer));

            // 2. Seed the exact records needed for the Python Test Suite
            FinancialRecord r1 = FinancialRecord.builder().amount(new BigDecimal("5000.00")).type(RecordType.INCOME).category("Salary").date(LocalDate.of(2026, 1, 5)).notes("Jan Salary").user(admin).build();
            FinancialRecord r2 = FinancialRecord.builder().amount(new BigDecimal("1200.00")).type(RecordType.EXPENSE).category("Rent").date(LocalDate.of(2026, 1, 6)).notes("Jan Rent").user(admin).build();
            FinancialRecord r3 = FinancialRecord.builder().amount(new BigDecimal("300.00")).type(RecordType.EXPENSE).category("Groceries").date(LocalDate.of(2026, 1, 15)).notes("Whole Foods").user(admin).build();

            FinancialRecord r4 = FinancialRecord.builder().amount(new BigDecimal("5000.00")).type(RecordType.INCOME).category("Salary").date(LocalDate.of(2026, 2, 5)).notes("Feb Salary").user(admin).build();
            FinancialRecord r5 = FinancialRecord.builder().amount(new BigDecimal("1200.00")).type(RecordType.EXPENSE).category("Rent").date(LocalDate.of(2026, 2, 6)).notes("Feb Rent").user(admin).build();

            FinancialRecord r6 = FinancialRecord.builder().amount(new BigDecimal("5000.00")).type(RecordType.INCOME).category("Salary").date(LocalDate.of(2026, 3, 5)).notes("Mar Salary").user(admin).build();
            FinancialRecord r7 = FinancialRecord.builder().amount(new BigDecimal("1200.00")).type(RecordType.EXPENSE).category("Rent").date(LocalDate.of(2026, 3, 6)).notes("Mar Rent").user(admin).build();

            FinancialRecord r8 = FinancialRecord.builder().amount(new BigDecimal("5000.00")).type(RecordType.INCOME).category("Salary").date(LocalDate.of(2026, 4, 5)).notes("Apr Salary").user(admin).build();
            FinancialRecord r9 = FinancialRecord.builder().amount(new BigDecimal("1200.00")).type(RecordType.EXPENSE).category("Rent").date(LocalDate.of(2026, 4, 6)).notes("Apr Rent").user(admin).build();

            // The Soft-Deleted Mistake Record
            FinancialRecord r10 = FinancialRecord.builder().amount(new BigDecimal("9999.99")).type(RecordType.EXPENSE).category("Mistake").date(LocalDate.of(2026, 4, 1)).notes("Should not appear").user(admin).isDeleted(true).build();

            recordRepository.saveAll(List.of(r1, r2, r3, r4, r5, r6, r7, r8, r9, r10));

            System.out.println("✅ Database seeded successfully with Test Data!");
        }
    }
}