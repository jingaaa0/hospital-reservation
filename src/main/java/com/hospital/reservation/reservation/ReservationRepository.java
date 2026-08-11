package com.hospital.reservation.reservation;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long>, JpaSpecificationExecutor<Reservation> {
    Optional<Reservation> findByReservationNumber(String reservationNumber);

    List<Reservation> findAllByAppointmentDateOrderByAppointmentTimeAsc(LocalDate appointmentDate);

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTime(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime appointmentTime
    );

    boolean existsByDoctorId(Long doctorId);
}
