package task.device.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import task.common.exceptions.InvalidMacAddressException;

class MacAddressTest {

    @ParameterizedTest
    @DisplayName("Should accept valid MAC address formats")
    @ValueSource(strings = {
            "00:1A:2B:3C:4D:5E", // Standard colon
            "00-1A-2B-3C-4D-5E", // Standard hyphen
            "001A.2B3C.4D5E",    // Cisco format
            "001A2B3C4D5E",      // Bare hex
            "ff:ff:ff:ff:ff:ff"  // Broadcast
    })
    void shouldCreateMacAddressForValidInput(String validMac) {
        MacAddress macAddress = new MacAddress(validMac);
        assertThat(macAddress.value()).isEqualTo(validMac);
    }

    @ParameterizedTest
    @DisplayName("Should throw exception for invalid MAC address formats")
    @ValueSource(strings = {
            "00:1A:2B:3C:4D",       // Too short
            "00:1A:2B:3C:4D:5E:6F", // Too long
            "G0:1A:2B:3C:4D:5E",    // Invalid hex (G)
            "",                     // Empty
            "   ",                  // Whitespace
            "not-a-mac"             // Random string
    })
    void shouldThrowExceptionForInvalidInput(String invalidMac) {
        var exception = assertThrows(InvalidMacAddressException.class, () -> new MacAddress(invalidMac));

        assertThat(exception.getDetails()).contains(invalidMac);
    }
}