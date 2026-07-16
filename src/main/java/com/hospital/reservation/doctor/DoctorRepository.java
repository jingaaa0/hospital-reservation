package com.hospital.reservation.doctor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findAllByActiveTrueOrderByDepartmentAscDisplayOrderAscNameAsc();

    List<Doctor> findAllByDepartmentAndActiveTrueOrderByDisplayOrderAscNameAsc(Department department);

    boolean existsByDepartmentAndName(Department department, String name);

    @Query("select distinct doctor.department from Doctor doctor where doctor.active = true order by doctor.department")
    List<Department> findActiveDepartments();
}
