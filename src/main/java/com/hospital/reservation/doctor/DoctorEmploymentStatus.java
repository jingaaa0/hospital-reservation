package com.hospital.reservation.doctor;

public enum DoctorEmploymentStatus {
    Y("재직 중"),
    L("휴직 중"),
    N("퇴사");

    private final String displayName;

    DoctorEmploymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getCode() {
        return name();
    }

    public String getDisplayName() {
        return displayName;
    }

    public static DoctorEmploymentStatus fromCode(String code) {
        return DoctorEmploymentStatus.valueOf(code);
    }
}
