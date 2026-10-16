package com.smishguard.smishguard;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** Context load test + API smoke tests using a real embedded server + RestClient. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("local")
class SmishguardApplicationTests {

    @LocalServerPort
    private int port;

    @Autowired
    private RestClient.Builder restBuilder;

    private RestClient client() {
        return restBuilder.baseUrl("http://localhost:" + port).build();
    }

    @Test
    void contextLoads() {
        // Spring context starts successfully if this passes
    }

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    void healthEndpointReturnsUp() {
        Map body = client().get().uri("/api/health").retrieve().body(Map.class);
        assertThat(body).isNotNull();
        assertThat(body.get("status")).isEqualTo("UP");
    }

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    void checkReturnsResultForFakeMessage() {
        String json = """
            {
              "message": "Your account is BLOCKED. Update KYC at http://sbi-kyc.xyz IMMEDIATELY!",
              "sender": "9876543210",
              "scanLinks": false,
              "useAi": false
            }
            """;
        Map body = client().post().uri("/api/check")
            .header("Content-Type", "application/json")
            .body(json)
            .retrieve()
            .body(Map.class);
        assertThat(body).isNotNull();
        assertThat(body.containsKey("verdict")).isTrue();
        assertThat(body.containsKey("riskScore")).isTrue();
        // Should NOT be GENUINE for an obvious scam
        assertThat(body.get("verdict")).isNotEqualTo("GENUINE");
    }

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    void checkReturnsResultForGenuineMessage() {
        String json = """
            {
              "message": "Rs.500.00 debited from A/c XXXXXX1234 on 05-Oct-26. Avl Bal Rs.9,500.00",
              "sender": "VK-HDFCBK",
              "scanLinks": false,
              "useAi": false
            }
            """;
        Map body = client().post().uri("/api/check")
            .header("Content-Type", "application/json")
            .body(json)
            .retrieve()
            .body(Map.class);
        assertThat(body).isNotNull();
        assertThat(body.containsKey("riskScore")).isTrue();
    }

    @Test
    void checkValidatesEmptyMessage() {
        assertThatThrownBy(() ->
            client().post().uri("/api/check")
                .header("Content-Type", "application/json")
                .body("{\"message\": \"\"}")
                .retrieve()
                .body(Map.class)
        ).isInstanceOf(HttpClientErrorException.BadRequest.class);
    }

    @Test
    void historyEndpointReturnsArray() {
        var body = client().get().uri("/api/history").retrieve().body(Object[].class);
        assertThat(body).isNotNull();
    }

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    void statsEndpointReturnsCounts() {
        Map body = client().get().uri("/api/stats").retrieve().body(Map.class);
        assertThat(body).isNotNull();
        assertThat(body.containsKey("total")).isTrue();
    }

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    void senderLookupKnownCode() {
        Map body = client().get().uri("/api/sender/HDFCBK").retrieve().body(Map.class);
        assertThat(body.get("known")).isEqualTo(true);
    }

    @Test
    @SuppressWarnings({"unchecked","rawtypes"})
    void senderLookupUnknownCode() {
        Map body = client().get().uri("/api/sender/NOTABANK").retrieve().body(Map.class);
        assertThat(body.get("known")).isEqualTo(false);
    }
}
