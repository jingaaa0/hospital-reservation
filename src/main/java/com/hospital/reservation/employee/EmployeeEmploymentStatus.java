package com.hospital.reservation.employee;

public enum EmployeeEmploymentStatus {
    Y("재직 중"),
    L("휴직 중"),
    N("퇴사");

    private final String displayName;

    EmployeeEmploymentStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
