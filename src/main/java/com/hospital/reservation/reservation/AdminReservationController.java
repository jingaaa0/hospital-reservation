package com.hospital.reservation.reservation;

import com.hospital.reservation.doctor.Department;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
public class AdminReservationController {

    private final AdminReservationService adminReservationService;

    public AdminReservationController(AdminReservationService adminReservationService) {
        this.adminReservationService = adminReservationService;
    }

    @GetMapping("/api/admin/reservations")
    public AdminReservationPageResponse findAll(
            @RequestParam(required = false) Department department,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) ReservationStatus status,
            @RequestParam(defaultValue = "appointment") String sortBy,
            @RequestParam(defaultValue = "ASC") Sort.Direction direction,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size
    ) {
        return adminReservationService.findAll(
                department, doctorId, date, status, sortBy, direction, page, size
        );
    }

    @PatchMapping("/api/admin/reservations/{reservationId}/status")
    public AdminReservationResponse updateStatus(
            @PathVariable Long reservationId,
            @Valid @RequestBody ReservationStatusUpdateRequest request
    ) {
        return adminReservationService.updateStatus(reservationId, request);
    }

    @PatchMapping("/api/admin/reservations/{reservationId}/memo")
    public AdminReservationResponse updateAdminMemo(
            @PathVariable Long reservationId,
            @Valid @RequestBody AdminMemoUpdateRequest request
    ) {
        return adminReservationService.updateAdminMemo(reservationId, request);
    }
}
