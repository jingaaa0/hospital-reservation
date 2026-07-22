package com.hospital.reservation.doctor;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    List<Doctor> findAllByEmploymentStatusOrderByDepartmentAscDisplayOrderAscNameAsc(
            DoctorEmploymentStatus employmentStatus
    );

    List<Doctor> findAllByDepartmentAndEmploymentStatusOrderByDisplayOrderAscNameAsc(
            Department department,
            DoctorEmploymentStatus employmentStatus
    );

    boolean existsByDepartmentAndName(Department department, String name);

    @Query("select distinct doctor.department from Doctor doctor where doctor.employmentStatus = :status order by doctor.department")
    List<Department> findDepartmentsByEmploymentStatus(@Param("status") DoctorEmploymentStatus status);
}
