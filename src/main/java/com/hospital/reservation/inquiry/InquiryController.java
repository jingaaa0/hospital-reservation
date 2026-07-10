package com.hospital.reservation.inquiry;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Controller: HTTP 요청, 응답 담당
@RestController // -> API 응답하겠다. @Controller + @ResponseBody
public class InquiryController {

    private final InquiryService inquiryService;

    public InquiryController(InquiryService inquiryService) {
        this.inquiryService = inquiryService;
    }

    // 문의하기 FORM 전송
    @PostMapping("/api/inquiries")
    @ResponseStatus(HttpStatus.CREATED)
    // @Valid = 입력값 검증
    public InquiryResponse create(@Valid @RequestBody InquiryRequest request) {
        Long inquiryId = inquiryService.create(request);
        return new InquiryResponse(inquiryId, "문의가 접수되었습니다. 확인 후 연락드리겠습니다.");
    }

    @GetMapping("/api/admin/inquiries")
    public List<InquirySummaryResponse> findAll() {
        return inquiryService.findAll();
    }

    @PatchMapping("/api/admin/inquiries/{inquiryId}/status")
    public InquirySummaryResponse updateStatus(
            @PathVariable Long inquiryId,
            @Valid @RequestBody InquiryStatusUpdateRequest request
    ) {
        return inquiryService.updateStatus(inquiryId, request);
    }
}
