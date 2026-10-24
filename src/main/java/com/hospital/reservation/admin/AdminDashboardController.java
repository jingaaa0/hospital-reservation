package com.hospital.reservation.admin;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.ZoneId;

@RestController // 변환한 DTO를 JSON 응답으로 전달
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(
            AdminDashboardService adminDashboardService
    ) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping("/api/admin/dashboard")
    public AdminDashboardResponse getDashboard() {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Seoul"));
        return adminDashboardService.getDashboard(today);
    }
}