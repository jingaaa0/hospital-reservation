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

                assertThat(
                        reservationRepository
                                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                                        doctor.getId(),
                                        appointmentDate,
                                        appointmentTime,
                                        ReservationStatus.CANCELLED
                                )
                ).isTrue();
        }

        @Test // 취소된 예약도 기존 중복 조회에 포함되는지 테스트
        void cancelledReservationIsIncludedInExistingSlotCheck() {
                // 1. 의사를 생성하고 doctorRepository에 저장
                Doctor doctor = doctorRepository.save(Doctor.create(
                        Department.INTERNAL_MEDICINE,
                        "테스트 의사",
                        LocalDate.of(1980, 1, 1),
                        Set.of(DayOfWeek.MONDAY),
                        1
                ));

                // 2. 해당 의사로 예약 객체 생성
                Reservation reservation = Reservation.create(
                        "테스트 환자",
                        "01012345678",
                        doctor,
                        LocalDate.of(2030, 1, 7),
                        LocalTime.of(10, 0),
                        null,
                        true
                );

                // 3. 예약 상태를 CANCELLED로 변경
                // 예약 생성 확인
                assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.REQUESTED);
                // 생성 -> 취소
                reservation.changeStatus(ReservationStatus.CANCELLED);
                // 취소로 변경되었는지 확인
                assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);

                // 4. reservationRepository.saveAndFlush()로 예약 저장
                reservationRepository.saveAndFlush(reservation);

                // 5. 같은 의사·날짜·시간으로 기존 중복 조회 → true인지 검증
                boolean exists =
                reservationRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTime(
                        doctor.getId(),
                        LocalDate.of(2030, 1, 7),
                        LocalTime.of(10, 0)
                );

                assertThat(exists).isTrue();

                boolean existsExcludingCancelled =
                        reservationRepository
                                .existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
                                        doctor.getId(),
                                        LocalDate.of(2030, 1, 7),
                                        LocalTime.of(10, 0),
                                        ReservationStatus.CANCELLED
                                );

                assertThat(existsExcludingCancelled).isFalse();
        }

}
