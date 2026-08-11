package com.hospital.reservation.employee;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    List<Employee> findAllByOrderByDepartmentAscDisplayOrderAscNameAsc();

    boolean existsByDepartmentAndName(EmployeeDepartment department, String name);

    boolean existsByDepartmentAndNameAndIdNot(EmployeeDepartment department, String name, Long id);
}
