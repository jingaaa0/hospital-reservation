package com.hospital.reservation.reservation;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public record AdminReservationResponse(
        Long reservationId,
        String reservationNumber,
        String patientName,
        String phoneNumber,
        LocalDate appointmentDate,
        LocalTime appointmentTime,
        String departmentCode,
        String departmentName,
        Long doctorId,
        String doctorName,
        String symptom,
        String adminMemo,
        ReservationStatus status,
        LocalDateTime createdAt
) {
    public static AdminReservationResponse from(Reservation reservation) {
        return new AdminReservationResponse(
                reservation.getId(),
                reservation.getReservationNumber(),
                reservation.getPatientName(),
                reservation.getPhoneNumber(),
                reservation.getAppointmentDate(),
                reservation.getAppointmentTime(),
                reservation.getDoctor().getDepartment().name(),
                reservation.getDoctor().getDepartment().getDisplayName(),
                reservation.getDoctor().getId(),
                reservation.getDoctor().getName(),
                reservation.getSymptom(),
                reservation.getAdminMemo(),
                reservation.getStatus(),
                reservation.getCreatedAt()
        );
    }
}
