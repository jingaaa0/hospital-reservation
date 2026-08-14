package com.hospital.reservation.reservation;

import com.hospital.reservation.doctor.Department;
import com.hospital.reservation.doctor.Doctor;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ReservationTest {

    @Test
    void requiresPrivacyAgreementWhenCreatingReservation() {
        assertThatThrownBy(() -> Reservation.create(
                "홍길동",
                "01012345678",
                doctor(),
                LocalDate.now().plusDays(7),
                LocalTime.of(10, 0),
                null,
                false
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("개인정보 수집 및 이용에 동의해야 합니다.");
    }

    @Test
    void trimsAdminMemoAndConvertsBlankMemoToNull() {
        Reservation reservation = reservation();

        reservation.changeAdminMemo("  전화 상담 완료  ");
        assertThat(reservation.getAdminMemo()).isEqualTo("전화 상담 완료");

        reservation.changeAdminMemo("   ");
        assertThat(reservation.getAdminMemo()).isNull();
    }

    private Reservation reservation() {
        return Reservation.create(
                "홍길동",
                "01012345678",
                doctor(),
                LocalDate.now().plusDays(7),
                LocalTime.of(10, 0),
                null,
                true
        );
    }

    private Doctor doctor() {
        return Doctor.create(
                Department.ENT,
                "테스트 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY),
                1
        );
    }
}
