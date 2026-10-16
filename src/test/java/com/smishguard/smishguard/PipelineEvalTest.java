package com.smishguard.smishguard;

import com.opencsv.CSVReader;
import com.smishguard.smishguard.model.CheckRequest;
import com.smishguard.smishguard.model.CheckResponse;
import com.smishguard.smishguard.service.VerdictService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;

import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Evaluation test: runs all 40 test messages through the pipeline (rules only, no network).
 * Prints accuracy, precision, recall, F1, and a confusion matrix.
 *
 * NOTE: Replace test_messages.csv with your own real, masked messages for a proper evaluation.
 * See src/main/resources/data/README.md for the CSV format.
 *
 * A FAKE classified as GENUINE is the worst error (false negative).
 * The test prints FAKE recall clearly and fails if it drops below 50%.
 */
@SpringBootTest
@ActiveProfiles("local")
class PipelineEvalTest {

    @Autowired
    private VerdictService verdictService;

    record TestCase(String message, String sender, String expectedLabel) {}

    @Test
    void evaluatePipeline() throws Exception {
        List<TestCase> cases = loadTestCases();
        System.out.println("\n=== SmishGuard Pipeline Evaluation ===");
        System.out.printf("Total test messages: %d%n%n", cases.size());

        int tp = 0, fp = 0, tn = 0, fn = 0;
        List<String> misclassified = new ArrayList<>();

        for (TestCase tc : cases) {
            CheckRequest req = new CheckRequest(tc.message(), tc.sender(), false, false);
            CheckResponse resp = verdictService.check(req);

            // Treat GENUINE as negative, FAKE/SUSPICIOUS as positive (scam)
            boolean expectedFake = "fake".equalsIgnoreCase(tc.expectedLabel());
            boolean predictedFake = !"GENUINE".equals(resp.verdict());

            if (expectedFake && predictedFake)       tp++;
            else if (!expectedFake && predictedFake) fp++;
            else if (!expectedFake && !predictedFake) tn++;
            else {
                fn++;
                misclassified.add(String.format("[MISSED FAKE] verdict=%s score=%d | %s",
                    resp.verdict(), resp.riskScore(), tc.message().substring(0, Math.min(80, tc.message().length()))));
            }

            if (!expectedFake && predictedFake) {
                misclassified.add(String.format("[FALSE ALARM] verdict=%s score=%d | %s",
                    resp.verdict(), resp.riskScore(), tc.message().substring(0, Math.min(80, tc.message().length()))));
            }
        }

        double accuracy  = (double)(tp + tn) / cases.size() * 100;
        double precision = tp + fp > 0 ? (double) tp / (tp + fp) * 100 : 0;
        double recall    = tp + fn > 0 ? (double) tp / (tp + fn) * 100 : 0; // FAKE recall
        double f1        = precision + recall > 0
            ? 2 * precision * recall / (precision + recall) : 0;

        System.out.println("Confusion Matrix (Fake = Positive):");
        System.out.printf("  TP (correctly caught fakes):   %d%n", tp);
        System.out.printf("  FP (genuine flagged as scam):  %d%n", fp);
        System.out.printf("  TN (correctly passed genuine): %d%n", tn);
        System.out.printf("  FN (missed fakes!):            %d%n%n", fn);

        System.out.printf("Accuracy:          %.1f%%%n", accuracy);
        System.out.printf("Precision (scam):  %.1f%%%n", precision);
        System.out.printf("Recall (FAKE):     %.1f%% ← key metric%n", recall);
        System.out.printf("F1 Score:          %.1f%%%n%n", f1);

        if (!misclassified.isEmpty()) {
            System.out.println("Misclassified messages:");
            misclassified.forEach(m -> System.out.println("  " + m));
        }

        // Fail the test if FAKE recall is below 50% (too many missed scams)
        assertTrue(recall >= 50.0,
            String.format("FAKE recall too low: %.1f%% – too many scams missed!", recall));
    }

    private List<TestCase> loadTestCases() throws Exception {
        List<TestCase> cases = new ArrayList<>();
        try (var reader = new CSVReader(new InputStreamReader(
                new ClassPathResource("test_messages.csv").getInputStream(), StandardCharsets.UTF_8))) {
            String[] row;
            while ((row = reader.readNext()) != null) {
                if (row.length >= 2) {
                    // Columns: message, label, type(ignored), bank(ignored), sender
                    String sender = row.length >= 5 ? row[4].trim() : null;
                    cases.add(new TestCase(row[0].trim(), sender, row[1].trim()));
                }
            }
        }
        return cases;
    }
}
