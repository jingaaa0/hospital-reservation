package com.hospital.reservation.employee;

import com.hospital.reservation.common.PhoneNumberNormalizer;
import com.hospital.reservation.doctor.Department;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public record EmployeeRequest(
        @NotBlank(message = "이름을 입력해 주세요.")
        @Size(max = 50, message = "이름은 50자 이내로 입력해 주세요.")
        String name,

        @NotBlank(message = "전화번호를 입력해 주세요.")
        String phoneNumber,

        @NotNull(message = "진료과를 선택해 주세요.")
        Department department,

        @NotNull(message = "의료진을 선택해 주세요.")
        Long doctorId,

        @NotNull(message = "날짜를 선택해 주세요.")
        @FutureOrPresent(message = "오늘 이후의 날짜를 선택해 주세요.")
        LocalDate date,

        @NotNull(message = "시간대를 선택해 주세요.")
        LocalTime appointmentTime,

        @Size(max = 2000, message = "증상은 2,000자 이내로 입력해 주세요.")
        String symptom,

        @NotNull(message = "개인정보 수집 및 이용에 동의해 주세요.")
        @AssertTrue(message = "개인정보 수집 및 이용에 동의해 주세요.")
        Boolean privacyAgreed
) {
    @AssertTrue(message = "전화번호는 010-0000-0000 형식으로 입력해 주세요.")
    public boolean isPhoneNumberValid() {
        return PhoneNumberNormalizer.isValid(phoneNumber);
    }
}
