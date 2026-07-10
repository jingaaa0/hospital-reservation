package com.hospital.reservation.inquiry;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

// Repository: DB 저장 담당
public interface InquiryRepository extends JpaRepository<Inquiry, Long> {

    List<Inquiry> findAllByOrderByCreatedAtDesc();
}
