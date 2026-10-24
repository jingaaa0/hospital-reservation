package com.hospital.reservation.admin;

import com.hospital.reservation.reservation.AdminReservationResponse;

import java.time.LocalDate;
import java.util.List;

// 데이터 전달용 객체 정의
public record AdminDashboardResponse(
        LocalDate date,                 // 집계 기준 날짜
        long totalReservations,         // 금일 진료 예약 전체 건수(취소포함)
        long requestedReservations,     // 금일 신청 상태 건수
        long confirmedReservations,     // 금일 확정 상태 건수
        long pendingInquiries,          // 전체 기간의 접수됨 + 처리 중 문의 건수
        List<AdminReservationResponse> reservations // 오늘 예약 목록 > 예약 시간 순
) {
}