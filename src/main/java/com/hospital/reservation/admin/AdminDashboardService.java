package com.hospital.reservation.admin;

import com.hospital.reservation.inquiry.InquiryRepository;
import com.hospital.reservation.inquiry.InquiryStatus;
import com.hospital.reservation.reservation.AdminReservationResponse;
import com.hospital.reservation.reservation.Reservation;
import com.hospital.reservation.reservation.ReservationRepository;
import com.hospital.reservation.reservation.ReservationStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true) // 조회용 트랜잭션을 묶음
public class AdminDashboardService {

    private final ReservationRepository reservationRepository;
    private final InquiryRepository inquiryRepository;

    public AdminDashboardService(
            ReservationRepository reservationRepository,
            InquiryRepository inquiryRepository
    ) {
        this.reservationRepository = reservationRepository;
        this.inquiryRepository = inquiryRepository;
    }

    public AdminDashboardResponse getDashboard(LocalDate date) {
        List<Reservation> reservations =
                reservationRepository
                        .findAllByAppointmentDateOrderByAppointmentTimeAsc(date);

        long requestedCount = reservations.stream()
                .filter(reservation ->
                        reservation.getStatus() == ReservationStatus.REQUESTED)
                .count();

        long confirmedCount = reservations.stream()
                .filter(reservation ->
                        reservation.getStatus() == ReservationStatus.CONFIRMED)
                .count();

        long pendingInquiryCount =
                inquiryRepository.countByStatus(InquiryStatus.RECEIVED)
                + inquiryRepository.countByStatus(InquiryStatus.IN_PROGRESS);

        List<AdminReservationResponse> reservationResponses =
                reservations.stream()
                        .map(AdminReservationResponse::from)
                        .toList();

        return new AdminDashboardResponse(
                date,
                reservations.size(),
                requestedCount,
                confirmedCount,
                pendingInquiryCount,
                reservationResponses
        );
    }
}