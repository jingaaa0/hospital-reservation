package com.hospital.reservation.reservation;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class ReservationController {

    @PostMapping("/api/reservations")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Map<String, String> create(@Valid @RequestBody ReservationRequest request) {
        return Map.of("message", "예약 신청이 접수되었습니다. 확인 후 확정 안내를 드리겠습니다.");
    }
}
