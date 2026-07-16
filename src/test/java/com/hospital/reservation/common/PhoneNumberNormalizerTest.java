package com.hospital.reservation.common;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PhoneNumberNormalizerTest {

    @Test
    void normalizesKoreanPhoneNumberToDigitsOnly() {
        assertEquals("01012345678", PhoneNumberNormalizer.normalize("010-1234-5678"));
    }

    @Test
    void acceptsOnlyValidKoreanPhoneNumberInput() {
        assertTrue(PhoneNumberNormalizer.isValid("010-1234-5678"));
        assertTrue(PhoneNumberNormalizer.isValid("01012345678"));
        assertFalse(PhoneNumberNormalizer.isValid("010-ABCD-5678"));
        assertFalse(PhoneNumberNormalizer.isValid("011-1234-5678"));
        assertFalse(PhoneNumberNormalizer.isValid("1234"));
    }
}
