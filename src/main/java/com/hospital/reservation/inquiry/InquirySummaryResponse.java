package com.hospital.reservation.inquiry;

import java.time.LocalDateTime;

public record InquirySummaryResponse(
        Long inquiryId,
        String name,
        String phoneNumber,
        String email,
        String content,
        InquiryStatus status,
        LocalDateTime createdAt
) {
    public static InquirySummaryResponse from(Inquiry inquiry) {
        return new InquirySummaryResponse(
                inquiry.getId(),
                inquiry.getName(),
                inquiry.getPhoneNumber(),
                inquiry.getEmail(),
                inquiry.getContent(),
                inquiry.getStatus(),
                inquiry.getCreatedAt()
        );
    }
}
