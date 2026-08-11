package com.hospital.reservation.employee;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

public record EmployeeSaveRequest(
        @NotNull(message = "직원 구분을 선택해 주세요.") EmployeeType type,
        @NotBlank(message = "이름을 입력해 주세요.") String name,
        @NotBlank(message = "부서 또는 진료과를 선택해 주세요.") String department,
        String position,
        @NotNull(message = "생년월일을 입력해 주세요.") LocalDate birthDate,
        @NotBlank(message = "재직 상태를 선택해 주세요.") String employmentStatus,
        @Min(value = 0, message = "노출 순서는 0 이상이어야 합니다.") int displayOrder,
        Set<DayOfWeek> availableDays
) {
}
