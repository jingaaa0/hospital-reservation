package com.hospital.reservation.inquiry;

import java.util.List;

public record InquiryPageResponse(
        List<InquirySummaryResponse> items,
        int page,
        int size,
        long totalElements,
        int totalPages,
        long receivedCount,
        long inProgressCount,
        long answeredCount
) {
}
