package com.hospital.reservation.reservation;

import com.hospital.reservation.doctor.Department;
import com.hospital.reservation.doctor.Doctor;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import lombok.extern.slf4j.Slf4j;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
class ReservationTest {

    @Test
    // 예약 신청 확정 테스트
    void requestedReservationCanBeConfirmed() {
        // 1. 준비: 의사와 신청 상태의 예약 생성
        Doctor doctor = Doctor.create(
                Department.INTERNAL_MEDICINE,
                "테스트 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY),
                1
        );

        Reservation reservation = Reservation.create(
                "테스트 환자",
                "01012345678",
                doctor,
                LocalDate.of(2030, 1, 7),
                LocalTime.of(10, 0),
                null,
                true
        );

        // assertThat(실제값).isEqualTo(기대값)
        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.REQUESTED);

        // 2. 실행: 확정 상태로 변경
        reservation.changeStatus(ReservationStatus.CONFIRMED);

        // 3. 검증: 실제로 확정 상태가 되었는지 확인
        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.CONFIRMED);
    }

    @Test
    // 예약 신청 불가 테스트
    void requestedReservationCannotBeCompleted() {
        // 0. 준비: 의사와 신청 상태의 예약 생성
        Doctor doctor = Doctor.create(
                Department.INTERNAL_MEDICINE,
                "테스트 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY),
                1
        );

        Reservation reservation = Reservation.create(
                "테스트 환자",
                "01012345678",
                doctor,
                LocalDate.of(2030, 1, 7),
                LocalTime.of(10, 0),
                null,
                true
        );

        // 1. 신청 → 완료 요청 시 예외가 발생하는지 검증
        assertThatThrownBy(() ->
                reservation.changeStatus(ReservationStatus.COMPLETED)
        ).isInstanceOf(ReservationException.class);

        // 2. 거절된 이후에도 신청 상태인지 검증
        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.REQUESTED);
    }

    @Test
    // 예약 확정 -> 완료 성공 테스트
    void confirmedReservationCanBeCompleted() {
        // 0. 준비: 의사와 신청 상태의 예약 생성
        Doctor doctor = Doctor.create(
                Department.INTERNAL_MEDICINE,
                "테스트 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY),
                1
        );

        Reservation reservation = Reservation.create(
                "테스트 환자",
                "01012345678",
                doctor,
                LocalDate.of(2030, 1, 7),
                LocalTime.of(10, 0),
                null,
                true
        );

        // 1. 준비: 신청 상태를 확정으로 변경
        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.REQUESTED);

        reservation.changeStatus(ReservationStatus.CONFIRMED);

        // 2. 실행: 완료 상태로 변경
        reservation.changeStatus(ReservationStatus.COMPLETED);

        // 3. 검증: 현재 상태가 COMPLETED인지 확인
        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.COMPLETED);
    }

    // 완료된 예약을 취소하려 하면 거절되고 완료 상태가 유지되는지 검증
    @Test
    void completedReservationCannotBeCancelled() {
        // 의사, 예약 준비
        Doctor doctor = Doctor.create(
                Department.INTERNAL_MEDICINE,
                "테스트 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY),
                1
        );

        Reservation reservation = Reservation.create(
                "테스트 환자",
                "01012345678",
                doctor,
                LocalDate.of(2030, 1, 7),
                LocalTime.of(10, 0),
                null,
                true
        );

        // 신청 -> 확정 -> 완료 변경
        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.REQUESTED); // 신청 검증

        reservation.changeStatus(ReservationStatus.CONFIRMED); // 확정
        reservation.changeStatus(ReservationStatus.COMPLETED); // 완료

        // 취소로 변경하면 예외처리 발생 검증
        assertThatThrownBy(() ->
                reservation.changeStatus(ReservationStatus.CANCELLED)
        ).isInstanceOf(ReservationException.class);

        // 기존 COMPLETED 유지 검증
        assertThat(reservation.getStatus())
            .isEqualTo(ReservationStatus.COMPLETED);
    }

    @Test // 예약 생성 -> 취소 검증
    void requestedReservationCanBeCancelled() {
        // 의사, 예약 준비
        Doctor doctor = Doctor.create(
                Department.INTERNAL_MEDICINE,
                "테스트 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY),
                1
        );

        Reservation reservation = Reservation.create(
                "테스트 환자",
                "01012345678",
                doctor,
                LocalDate.of(2030, 1, 7),
                LocalTime.of(10, 0),
                null,
                true
        );

        // 신청 상태 확인
        assertThat(reservation.getStatus())
                .isEqualTo(ReservationStatus.REQUESTED); // 신청 검증

        reservation.changeStatus(ReservationStatus.CANCELLED);

        // 취소 상태로 변경됐는지 검증
        assertThat(reservation.getStatus())
            .isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test // 예약 생성 -> 확정 -> 취소 검증
    void confirmedReservationCanBeCancelled() {
        // 의사, 예약 준비
        Doctor doctor = Doctor.create(
                Department.INTERNAL_MEDICINE,
                "테스트 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY),
                1
        );

        Reservation reservation = Reservation.create(
                "테스트 환자",
                "01012345678",
                doctor,
                LocalDate.of(2030, 1, 7),
                LocalTime.of(10, 0),
                null,
                true
        );

        // 예약 생성 확인
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.REQUESTED);

        // 확정 → 취소로 변경
        reservation.changeStatus(ReservationStatus.CONFIRMED);
        reservation.changeStatus(ReservationStatus.CANCELLED);

        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test // 예약 생성 -> 확정 -> 미방문 검증
    void confirmedReservationCanBeMarkedNoShow() {
        // 의사, 예약 준비
        Doctor doctor = Doctor.create(
                Department.INTERNAL_MEDICINE,
                "테스트 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY),
                1
        );

        Reservation reservation = Reservation.create(
                "테스트 환자",
                "01012345678",
                doctor,
                LocalDate.of(2030, 1, 7),
                LocalTime.of(10, 0),
                null,
                true
        );

        // 예약 생성 확인
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.REQUESTED);

        reservation.changeStatus(ReservationStatus.CONFIRMED);
        reservation.changeStatus(ReservationStatus.NO_SHOW);

        // 미방문 상태로 변경됐는지 검증
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.NO_SHOW);
    }

    @Test // 예약 생성 -> 취소
    void cancelledReservationCannotBeConfirmed() {
        // 의사, 예약 준비
        Doctor doctor = Doctor.create(
                Department.INTERNAL_MEDICINE,
                "테스트 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY),
                1
        );

        Reservation reservation = Reservation.create(
                "테스트 환자",
                "01012345678",
                doctor,
                LocalDate.of(2030, 1, 7),
                LocalTime.of(10, 0),
                null,
                true
        );

        // 예약 생성 확인
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.REQUESTED);
        // 생성 -> 취소
        reservation.changeStatus(ReservationStatus.CANCELLED);
        // 취소 후 다시 확정으로 변경
        assertThatThrownBy(() ->
                reservation.changeStatus(ReservationStatus.CONFIRMED)
        ).isInstanceOf(ReservationException.class);

        // 확정 변경 거절하고 취소 유지되는지 검증
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.CANCELLED);
    }

    @Test
    void noShowReservationCannotBeCompleted() {
        // 의사, 예약 준비
        Doctor doctor = Doctor.create(
                Department.INTERNAL_MEDICINE,
                "테스트 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY),
                1
        );

        Reservation reservation = Reservation.create(
                "테스트 환자",
                "01012345678",
                doctor,
                LocalDate.of(2030, 1, 7),
                LocalTime.of(10, 0),
                null,
                true
        );

        // 예약 생성 확인
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.REQUESTED);
        // 생성 -> 확정
        reservation.changeStatus(ReservationStatus.CONFIRMED);
        // 확정 -> 미방문
        reservation.changeStatus(ReservationStatus.NO_SHOW);
        // 미방문 -> 완료
        assertThatThrownBy(() ->
                reservation.changeStatus(ReservationStatus.COMPLETED)
        ).isInstanceOf(ReservationException.class);

        // 완료 변경 거절하고 미방문 유지되는지 검증
        assertThat(reservation.getStatus()).isEqualTo(ReservationStatus.NO_SHOW);
    }
} 