package com.hospital.reservation.inquiry;

import com.hospital.reservation.common.PhoneNumberNormalizer;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

// JSON 데이터 변환·검증
public record InquiryRequest(
        @NotBlank(message = "이름을 입력해 주세요.") String name,
        String phoneNumber,
        @Email(message = "이메일 형식을 확인해 주세요.") String email,
        @NotBlank(message = "문의 내용을 입력해 주세요.") String content,
        Boolean privacyAgreed
) {
    @AssertTrue(message = "전화번호 또는 이메일 중 하나를 입력해 주세요.")
    public boolean isContactMethodProvided() {
        return (phoneNumber != null && !phoneNumber.isBlank()) || (email != null && !email.isBlank());
    }

    @AssertTrue(message = "전화번호는 010-0000-0000 형식으로 입력해 주세요.")
    public boolean isPhoneNumberValid() {
        return PhoneNumberNormalizer.isValid(phoneNumber);
    }

    @AssertTrue(message = "개인정보 수집 및 이용에 동의해 주세요.")
    public boolean isPrivacyAgreed() {
        return Boolean.TRUE.equals(privacyAgreed);
    }
}
