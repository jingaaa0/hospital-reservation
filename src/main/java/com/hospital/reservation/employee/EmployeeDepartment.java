package com.hospital.reservation.employee;

public enum EmployeeDepartment {
    NURSING("간호팀"),
    ADMINISTRATION("원무팀"),
    LABORATORY("검사실");

    private final String displayName;

    EmployeeDepartment(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
