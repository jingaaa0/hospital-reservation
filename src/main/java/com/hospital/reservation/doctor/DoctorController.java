package com.hospital.reservation.doctor;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/api/departments")
    public List<DepartmentResponse> findDepartments() {
        return doctorService.findDepartments();
    }

    @GetMapping("/api/doctors")
    public List<DoctorResponse> findDoctors(@RequestParam(required = false) Department department) {
        return doctorService.findDoctors(department);
    }
}
