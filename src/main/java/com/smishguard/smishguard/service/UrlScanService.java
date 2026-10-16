package com.smishguard.smishguard.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.http.MediaType;

import java.util.*;

/**
 * Step 3 of the pipeline – Google Safe Browsing API check.
 * Only called when scanLinks=true and URLs were found.
 * If the API key is missing, skips gracefully.
 * NEVER fetches the suspicious URL itself.
 */
@Service
public class UrlScanService {

    private static final Logger log = LoggerFactory.getLogger(UrlScanService.class);
    private static final String SAFE_BROWSING_URL =
        "https://safebrowsing.googleapis.com/v4/threatMatches:find?key=";

    @Value("${app.safe-browsing-api-key:}")
    private String apiKey;

    private final RestClient restClient;

    public UrlScanService(RestClient.Builder builder) {
        this.restClient = builder.build();
    }

    public record ScanResult(boolean malicious, String status) {}

    /**
     * Checks each URL against Google Safe Browsing.
     * Returns malicious=true + status="malicious" if any match found.
     */
    public ScanResult scan(List<String> urls) {
        if (urls.isEmpty()) return new ScanResult(false, "no links");
        if (apiKey == null || apiKey.isBlank()) {
            return new ScanResult(false, "Link scan not available (no API key configured)");
        }

        try {
            // Build request body as a Map (Jackson will serialize it)
            Map<String, Object> client = Map.of(
                "clientId", "smishguard",
                "clientVersion", "1.0"
            );
            List<Map<String, String>> threatTypes = List.of(
                Map.of("threatType", "MALWARE"),
                Map.of("threatType", "SOCIAL_ENGINEERING"),
                Map.of("threatType", "UNWANTED_SOFTWARE")
            );
            List<Map<String, String>> urlEntries = urls.stream()
                .map(u -> Map.of("url", u)).toList();
            Map<String, Object> body = Map.of(
                "client", client,
                "threatInfo", Map.of(
                    "threatTypes", threatTypes.stream().map(m -> m.get("threatType")).toList(),
                    "platformTypes", List.of("ANY_PLATFORM"),
                    "threatEntryTypes", List.of("URL"),
                    "threatEntries", urlEntries
                )
            );

            @SuppressWarnings("unchecked")
            Map<String, Object> response = restClient.post()
                .uri(SAFE_BROWSING_URL + apiKey)
                .contentType(MediaType.APPLICATION_JSON)
                .body(body)
                .retrieve()
                .body(Map.class);

            // If "matches" key is present and non-empty, at least one URL is malicious
            if (response != null && response.containsKey("matches")) {
                @SuppressWarnings("unchecked")
                List<?> matches = (List<?>) response.get("matches");
                if (matches != null && !matches.isEmpty()) {
                    return new ScanResult(true, "malicious");
                }
            }
            return new ScanResult(false, "clean");

        } catch (Exception e) {
            log.warn("Safe Browsing API call failed: {}", e.getMessage());
            return new ScanResult(false, "Link scan failed (service error)");
        }
    }
}
