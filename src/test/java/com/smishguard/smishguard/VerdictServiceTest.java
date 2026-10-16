package com.smishguard.smishguard;

import com.smishguard.smishguard.service.VerdictService;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;

/** Unit tests for VerdictService static helper methods. */
class VerdictServiceTest {

    @Test
    void scoreZeroToTwentyNineIsGenuine() {
        assertThat(VerdictService.scoreToVerdict(0)).isEqualTo("GENUINE");
        assertThat(VerdictService.scoreToVerdict(15)).isEqualTo("GENUINE");
        assertThat(VerdictService.scoreToVerdict(29)).isEqualTo("GENUINE");
    }

    @Test
    void scoreThirtyToFiftyNineIsSuspicious() {
        assertThat(VerdictService.scoreToVerdict(30)).isEqualTo("SUSPICIOUS");
        assertThat(VerdictService.scoreToVerdict(45)).isEqualTo("SUSPICIOUS");
        assertThat(VerdictService.scoreToVerdict(59)).isEqualTo("SUSPICIOUS");
    }

    @Test
    void scoreSixtyPlusIsFake() {
        assertThat(VerdictService.scoreToVerdict(60)).isEqualTo("FAKE");
        assertThat(VerdictService.scoreToVerdict(100)).isEqualTo("FAKE");
    }
}
