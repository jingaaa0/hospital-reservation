package com.hospital.reservation.employee;

import com.hospital.reservation.doctor.Doctor;

import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.Set;

public record EmployeeResponse(
        Long employeeId,
        EmployeeType type,
        String name,
        String department,
        String position,
        LocalDate birthDate,
        String employmentStatus,
        int displayOrder,
        Set<DayOfWeek> availableDays
) {
    static EmployeeResponse from(Doctor doctor) {
        return new EmployeeResponse(
                doctor.getId(),
                EmployeeType.DOCTOR,
                doctor.getName(),
                doctor.getDepartment().getDisplayName(),
                "의사",
                doctor.getBirthDate(),
                doctor.getEmploymentStatus().name(),
                doctor.getDisplayOrder(),
                Set.copyOf(doctor.getAvailableDays())
        );
    }

    static EmployeeResponse from(Employee employee) {
        return new EmployeeResponse(
                employee.getId(),
                EmployeeType.STAFF,
                employee.getName(),
                employee.getDepartment().getDisplayName(),
                employee.getPosition(),
                employee.getBirthDate(),
                employee.getEmploymentStatus().name(),
                employee.getDisplayOrder(),
                Set.of()
        );
    }
}
