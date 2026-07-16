package com.hospital.reservation.reservation;

import jakarta.validation.constraints.Size;

public record AdminMemoUpdateRequest(
        @Size(max = 2000, message = "관리자 메모는 2,000자 이하로 입력해 주세요.") String adminMemo
) {
}
