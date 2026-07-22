package com.hospital.reservation.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class EmployeeRepositoryTest {

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void savesEmployeeWithDepartmentAndPosition() {
        Employee employee = Employee.create(
                EmployeeDepartment.NURSING,
                "최서윤",
                "간호사",
                LocalDate.of(1992, 6, 17),
                1
        );

        Employee savedEmployee = employeeRepository.saveAndFlush(employee);

        assertThat(savedEmployee.getId()).isNotNull();
        assertThat(savedEmployee.getDepartment()).isEqualTo(EmployeeDepartment.NURSING);
        assertThat(savedEmployee.getPosition()).isEqualTo("간호사");
        assertThat(savedEmployee.getEmploymentStatus()).isEqualTo(EmployeeEmploymentStatus.Y);
        assertThat(savedEmployee.getCreatedAt()).isNotNull();
        assertThat(savedEmployee.getUpdatedAt()).isNotNull();
    }
}
