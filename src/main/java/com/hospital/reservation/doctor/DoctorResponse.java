package com.hospital.reservation.doctor;

public record DoctorResponse(Long doctorId, String name, String departmentCode, String departmentName) {
    public static DoctorResponse from(Doctor doctor) {
        return new DoctorResponse(
                doctor.getId(),
                doctor.getName(),
                doctor.getDepartment().name(),
                doctor.getDepartment().getDisplayName()
        );
    }
}
