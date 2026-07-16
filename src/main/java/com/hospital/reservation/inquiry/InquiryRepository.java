package com.hospital.reservation.inquiry;

import org.springframework.data.jpa.repository.JpaRepository;

// Repository: DB 저장 담당
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    long countByStatus(InquiryStatus status);
}
