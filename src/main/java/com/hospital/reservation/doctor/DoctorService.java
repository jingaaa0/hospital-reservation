package com.hospital.reservation.doctor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Comparator;

@Service
@Transactional(readOnly = true)
public class DoctorService {

    private final DoctorRepository doctorRepository;

    public DoctorService(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    public List<DepartmentResponse> findDepartments() {
        return doctorRepository.findDepartmentsByEmploymentStatus(DoctorEmploymentStatus.Y).stream()
                .sorted(Comparator.comparingInt(Department::ordinal))
                .map(DepartmentResponse::from)
                .toList();
    }

    public List<DoctorResponse> findDoctors(Department department) {
        List<Doctor> doctors = department == null
                ? doctorRepository.findAllByEmploymentStatusOrderByDepartmentAscDisplayOrderAscNameAsc(
                        DoctorEmploymentStatus.Y
                )
                : doctorRepository.findAllByDepartmentAndEmploymentStatusOrderByDisplayOrderAscNameAsc(
                        department,
                        DoctorEmploymentStatus.Y
                );
        return doctors.stream()
                .map(DoctorResponse::from)
                .toList();
    }
}
