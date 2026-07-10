package com.hospital.reservation.reservation;

import com.hospital.reservation.common.PhoneNumberNormalizer;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;

public record ReservationRequest(
        @NotBlank(message = "이름을 입력해 주세요.") String name,
        @NotBlank(message = "전화번호를 입력해 주세요.") String phoneNumber,
        @NotNull(message = "날짜를 선택해 주세요.") LocalDate date,
        @NotBlank(message = "시간대를 선택해 주세요.") String timeSlot,
        String note
) {
    @AssertTrue(message = "전화번호는 010-0000-0000 형식으로 입력해 주세요.")
    public boolean isPhoneNumberValid() {
        return PhoneNumberNormalizer.isValid(phoneNumber);
    }
}
