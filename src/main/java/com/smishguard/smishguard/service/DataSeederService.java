package com.smishguard.smishguard.service;

import com.opencsv.CSVReader;
import com.smishguard.smishguard.entity.*;
import com.smishguard.smishguard.repository.*;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * Runs once on startup. Loads seed data from CSV files in src/main/resources/data/
 * only when each table is empty (idempotent).
 *
 * CSV format: no header row assumed – first column is always bank_name (or message_text).
 * See data/README.md for full column documentation.
 */
@Service
public class DataSeederService implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeederService.class);

    private final BankSenderIdRepository    senderRepo;
    private final TrustedDomainRepository   domainRepo;
    private final GenuineTemplateRepository templateRepo;
    private final ReferenceMessageRepository refRepo;

    public DataSeederService(BankSenderIdRepository senderRepo,
                             TrustedDomainRepository domainRepo,
                             GenuineTemplateRepository templateRepo,
                             ReferenceMessageRepository refRepo) {
        this.senderRepo   = senderRepo;
        this.domainRepo   = domainRepo;
        this.templateRepo = templateRepo;
        this.refRepo      = refRepo;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        seedSenders();
        seedDomains();
        seedTemplates();
        seedReferenceMessages();
        log.info("Seed data check complete.");
    }

    // ── Seed methods ────────────────────────────────────────────────────

    private void seedSenders() {
        if (senderRepo.count() > 0) return;
        // Columns: bank_name, sender_code
        readCsv("data/bank_sender_ids.csv").forEach(row -> {
            if (row.length >= 2)
                senderRepo.save(new BankSenderId(row[0].trim(), row[1].trim().toUpperCase()));
        });
        log.info("Seeded {} bank sender IDs", senderRepo.count());
    }

    private void seedDomains() {
        if (domainRepo.count() > 0) return;
        // Columns: bank_name, domain
        readCsv("data/trusted_domains.csv").forEach(row -> {
            if (row.length >= 2)
                domainRepo.save(new TrustedDomain(row[0].trim(), row[1].trim().toLowerCase()));
        });
        log.info("Seeded {} trusted domains", domainRepo.count());
    }

    private void seedTemplates() {
        if (templateRepo.count() > 0) return;
        // Columns: bank_name, type, template_text
        readCsv("data/genuine_templates.csv").forEach(row -> {
            if (row.length >= 3)
                templateRepo.save(new GenuineTemplate(row[0].trim(), row[1].trim(), row[2].trim()));
        });
        log.info("Seeded {} genuine templates", templateRepo.count());
    }

    private void seedReferenceMessages() {
        if (refRepo.count() > 0) return;
        // Columns: message_text, label, type, bank, sender
        readCsv("data/reference_messages.csv").forEach(row -> {
            if (row.length >= 5)
                refRepo.save(new ReferenceMessage(
                    row[0].trim(), row[1].trim(), row[2].trim(),
                    row[3].trim(), row[4].trim()
                ));
        });
        log.info("Seeded {} reference messages", refRepo.count());
    }

    /** Reads a classpath CSV resource and returns rows as String arrays. */
    private List<String[]> readCsv(String classpathPath) {
        try (var reader = new CSVReader(new InputStreamReader(
                new ClassPathResource(classpathPath).getInputStream(), StandardCharsets.UTF_8))) {
            return reader.readAll();
        } catch (Exception e) {
            log.error("Failed to read seed file {}: {}", classpathPath, e.getMessage());
            return List.of();
        }
    }
}
