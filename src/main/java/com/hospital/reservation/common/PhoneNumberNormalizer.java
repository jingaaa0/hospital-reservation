package com.hospital.reservation.common;

public final class PhoneNumberNormalizer {

    private PhoneNumberNormalizer() {
    }

    public static String normalize(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return null;
        }
        return phoneNumber.replaceAll("[^0-9]", "");
    }

    public static boolean isValid(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.isBlank()) {
            return true;
        }
        if (!phoneNumber.matches("[0-9\\s-]+")) {
            return false;
        }
        return normalize(phoneNumber).matches("010\\d{8}");
    }
}
