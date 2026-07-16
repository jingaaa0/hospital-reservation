package com.hospital.reservation.doctor;

public enum Department {
    ENT("이비인후과"),
    INTERNAL_MEDICINE("내과"),
    ORTHOPEDICS("정형외과"),
    GENERAL_SURGERY("외과"),
    NEUROSURGERY("신경외과"),
    REHABILITATION_MEDICINE("재활의학과");

    private final String displayName;

    Department(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
