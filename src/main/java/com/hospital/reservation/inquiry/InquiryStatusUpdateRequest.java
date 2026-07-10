package com.hospital.reservation.inquiry;

import jakarta.validation.constraints.NotNull;

public record InquiryStatusUpdateRequest(
        @NotNull(message = "문의 상태를 선택해 주세요.") InquiryStatus status
) {
}
