package com.hospital.reservation.doctor;

public record DepartmentResponse(String code, String name) {
    public static DepartmentResponse from(Department department) {
        return new DepartmentResponse(department.name(), department.getDisplayName());
    }
}
