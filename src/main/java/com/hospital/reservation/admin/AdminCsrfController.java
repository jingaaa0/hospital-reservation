package com.hospital.reservation.admin;

import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AdminCsrfController {

    @GetMapping("/api/admin/csrf")
    public AdminCsrfResponse csrf(CsrfToken csrfToken) {
        return new AdminCsrfResponse(csrfToken.getHeaderName(), csrfToken.getToken());
    }

    public record AdminCsrfResponse(String headerName, String token) {
    }
}
