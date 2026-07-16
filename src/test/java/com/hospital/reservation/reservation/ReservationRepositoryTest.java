package com.hospital.reservation.reservation;

import com.hospital.reservation.doctor.Department;
import com.hospital.reservation.doctor.Doctor;
import com.hospital.reservation.doctor.DoctorRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class ReservationRepositoryTest {

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Test
    void savesReservationWithDoctor() {
        Doctor doctor = doctorRepository.save(Doctor.create(
                Department.INTERNAL_MEDICINE,
                "테스트 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY),
                1
        ));
        LocalDate appointmentDate = LocalDate.of(2026, 8, 3);
        LocalTime appointmentTime = LocalTime.of(10, 30);

        Reservation reservation = reservationRepository.saveAndFlush(Reservation.create(
                "홍길동",
                "01012345678",
                doctor,
                appointmentDate,
                appointmentTime,
                "두통이 있습니다.",
                true
        ));

        assertThat(reservation.getId()).isNotNull();
        assertThat(reservation.getReservationNumber()).startsWith("RSV-");
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.REQUESTED);
        assertThat(reservation.getDoctor().getId()).isEqualTo(doctor.getId());
        assertThat(reservationRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                doctor.getId(), appointmentDate, appointmentTime
        )).isTrue();
    }
}
