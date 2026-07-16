package com.hospital.reservation.reservation;

import java.util.List;

public record AdminReservationPageResponse(
        List<AdminReservationResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages
) {
}
