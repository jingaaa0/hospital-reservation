package com.hospital.reservation.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPageController {

    @GetMapping({"/admin", "/admin/"})
    public String adminPage() {
        return "admin/inquiries";
    }

    @GetMapping("/admin/login")
    public String loginPage() {
        return "admin/login";
    }

    @GetMapping("/admin/reservations")
    public String reservationPage() {
        return "admin/reservations";
    }

    @GetMapping("/admin/employees")
    public String employeePage() {
        return "admin/employees";
    }
}
