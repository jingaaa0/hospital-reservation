package com.hospital.reservation.inquiry;

import com.hospital.reservation.common.PhoneNumberNormalizer;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

import static org.springframework.http.HttpStatus.NOT_FOUND;

// Service: 문의 생성 업무 담당
@Service
public class InquiryService {

    private final InquiryRepository inquiryRepository;

    public InquiryService(InquiryRepository inquiryRepository) {
        this.inquiryRepository = inquiryRepository;
    }

    @Transactional
    public Long create(InquiryRequest request) {
        Inquiry inquiry = Inquiry.create(
                request.name(),
                PhoneNumberNormalizer.normalize(request.phoneNumber()),
                request.email(),
                request.content()
        );
        return inquiryRepository.save(inquiry).getId();
    }

    @Transactional(readOnly = true)
    public List<InquirySummaryResponse> findAll() {
        return inquiryRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(InquirySummaryResponse::from)
                .toList();
    }

    @Transactional
    public InquirySummaryResponse updateStatus(Long inquiryId, InquiryStatusUpdateRequest request) {
        Inquiry inquiry = inquiryRepository.findById(inquiryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND, "문의를 찾을 수 없습니다."));
        inquiry.changeStatus(request.status());
        return InquirySummaryResponse.from(inquiry);
    }
}
