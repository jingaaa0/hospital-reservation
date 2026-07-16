package com.hospital.reservation.reservation;

import jakarta.validation.constraints.NotNull;

public record ReservationStatusUpdateRequest(
        @NotNull(message = "예약 상태를 선택해 주세요.") ReservationStatus status
) {
}
