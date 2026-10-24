package com.hospital.reservation.admin;

import com.hospital.reservation.doctor.DoctorRepository;
import com.hospital.reservation.inquiry.InquiryRepository;
import com.hospital.reservation.reservation.ReservationRepository;
import com.hospital.reservation.doctor.Department;
import com.hospital.reservation.doctor.Doctor;
import com.hospital.reservation.inquiry.Inquiry;
import com.hospital.reservation.inquiry.InquiryStatus;
import com.hospital.reservation.reservation.AdminReservationResponse;
import com.hospital.reservation.reservation.Reservation;
import com.hospital.reservation.reservation.ReservationStatus;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(AdminDashboardService.class)
class AdminDashboardServiceTest {

    @Autowired
    private AdminDashboardService dashboardService;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private InquiryRepository inquiryRepository;

    @Test
    void returnsDateScopedCountsAndTimeOrderedReservations() {
        // 준비: 기준 날짜와 의사
        LocalDate date = LocalDate.of(2030, 1, 7);

        Doctor doctor = doctorRepository.save(Doctor.create(
                Department.INTERNAL_MEDICINE,
                "대시보드 테스트 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.TUESDAY),
                1
        ));

        Reservation requested = Reservation.create(
                "신청 환자", "01012345678", doctor,
                date, LocalTime.of(11, 0), null, true
        );

        Reservation confirmed = Reservation.create(
                "확정 환자", "01012345678", doctor,
                date, LocalTime.of(9, 0), null, true
        );
        confirmed.changeStatus(ReservationStatus.CONFIRMED);

        Reservation cancelled = Reservation.create(
                "취소 환자", "01012345678", doctor,
                date, LocalTime.of(10, 0), null, true
        );
        cancelled.changeStatus(ReservationStatus.CANCELLED);

        Reservation otherDate = Reservation.create(
                "다른 날짜 환자", "01012345678", doctor,
                date.plusDays(1), LocalTime.of(9, 0), null, true
        );

        // 일부러 예약 시간순과 다르게 저장
        reservationRepository.saveAllAndFlush(
                List.of(requested, confirmed, cancelled, otherDate)
        );

        Inquiry received = Inquiry.create(
                "접수 문의", "01012345678", null, "접수된 문의"
        );

        Inquiry inProgress = Inquiry.create(
                "처리 중 문의", "01012345678", null, "처리 중인 문의"
        );
        inProgress.changeStatus(InquiryStatus.IN_PROGRESS);

        Inquiry answered = Inquiry.create(
                "완료 문의", "01012345678", null, "답변 완료 문의"
        );
        answered.changeStatus(InquiryStatus.ANSWERED);

        inquiryRepository.saveAllAndFlush(
                List.of(received, inProgress, answered)
        );

        // 실행
        AdminDashboardResponse result = dashboardService.getDashboard(date);

        // 검증
        assertThat(result.date()).isEqualTo(date);
        assertThat(result.totalReservations()).isEqualTo(3);
        assertThat(result.requestedReservations()).isEqualTo(1);
        assertThat(result.confirmedReservations()).isEqualTo(1);
        assertThat(result.pendingInquiries()).isEqualTo(2);

        assertThat(result.reservations())
                .extracting(AdminReservationResponse::appointmentDate)
                .containsOnly(date);

        assertThat(result.reservations())
                .extracting(AdminReservationResponse::reservationId)
                .containsExactly(
                        confirmed.getId(),
                        cancelled.getId(),
                        requested.getId()
                );
    }
}