package com.hospital.reservation.employee;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class EmployeeBootstrapConfig implements ApplicationRunner {

    private final EmployeeRepository employeeRepository;

    public EmployeeBootstrapConfig(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (employeeRepository.count() > 0) {
            return;
        }

        Employee nurse = employee(EmployeeDepartment.NURSING, "최서윤", "간호사", 1992, 6, 17, 1);
        Employee administrator = employee(EmployeeDepartment.ADMINISTRATION, "정하람", "원무 담당", 1995, 1, 28, 1);
        Employee technician = employee(EmployeeDepartment.LABORATORY, "윤지안", "임상병리사", 1990, 9, 9, 1);
        administrator.changeEmploymentStatus(EmployeeEmploymentStatus.L);
        technician.changeEmploymentStatus(EmployeeEmploymentStatus.N);

        employeeRepository.saveAll(List.of(nurse, administrator, technician));
    }

    private Employee employee(
            EmployeeDepartment department,
            String name,
            String position,
            int year,
            int month,
            int day,
            int displayOrder
    ) {
        return Employee.create(department, name, position, LocalDate.of(year, month, day), displayOrder);
    }
}
