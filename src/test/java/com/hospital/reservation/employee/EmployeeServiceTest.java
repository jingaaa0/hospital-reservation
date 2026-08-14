package com.hospital.reservation.employee;

import com.hospital.reservation.doctor.Department;
import com.hospital.reservation.doctor.Doctor;
import com.hospital.reservation.doctor.DoctorRepository;
import com.hospital.reservation.reservation.ReservationRepository;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EmployeeServiceTest {

    private final DoctorRepository doctorRepository = mock(DoctorRepository.class);
    private final EmployeeRepository employeeRepository = mock(EmployeeRepository.class);
    private final ReservationRepository reservationRepository = mock(ReservationRepository.class);
    private final EmployeeService employeeService = new EmployeeService(
            doctorRepository,
            employeeRepository,
            reservationRepository
    );

    @Test
    void rejectsDuplicateEmployeeInSameDepartment() {
        EmployeeSaveRequest request = staffRequest("홍길동", "간호사");
        when(employeeRepository.existsByDepartmentAndName(EmployeeDepartment.NURSING, "홍길동"))
                .thenReturn(true);

        assertEmployeeError(() -> employeeService.create(request), "DUPLICATE_EMPLOYEE");
        verify(employeeRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void requiresPositionForGeneralStaff() {
        EmployeeSaveRequest request = staffRequest("홍길동", "  ");

        assertEmployeeError(() -> employeeService.create(request), "POSITION_REQUIRED");
    }

    @Test
    void requiresAtLeastOneAvailableDayForDoctor() {
        EmployeeSaveRequest request = new EmployeeSaveRequest(
                EmployeeType.DOCTOR,
                "홍길동",
                "ENT",
                null,
                LocalDate.of(1980, 1, 1),
                "Y",
                1,
                Set.of()
        );

        assertEmployeeError(() -> employeeService.create(request), "AVAILABLE_DAYS_REQUIRED");
    }

    @Test
    void doesNotAllowChangingEmployeeTypeDuringUpdate() {
        EmployeeSaveRequest request = staffRequest("홍길동", "간호사");

        assertEmployeeError(() -> employeeService.update(EmployeeType.DOCTOR, 1L, request), "TYPE_MISMATCH");
    }

    @Test
    void protectsDoctorWithReservationHistoryFromDeletion() {
        Doctor doctor = Doctor.create(
                Department.ENT,
                "홍길동",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY),
                1
        );
        when(doctorRepository.findById(1L)).thenReturn(Optional.of(doctor));
        when(reservationRepository.existsByDoctorId(1L)).thenReturn(true);

        assertEmployeeError(() -> employeeService.delete(EmployeeType.DOCTOR, 1L), "DOCTOR_HAS_RESERVATIONS");
        verify(doctorRepository, never()).delete(doctor);
    }

    private EmployeeSaveRequest staffRequest(String name, String position) {
        return new EmployeeSaveRequest(
                EmployeeType.STAFF,
                name,
                "NURSING",
                position,
                LocalDate.of(1990, 1, 1),
                "Y",
                1,
                Set.of()
        );
    }

    private void assertEmployeeError(Runnable operation, String code) {
        assertThatThrownBy(operation::run)
                .isInstanceOf(EmployeeException.class)
                .extracting(exception -> ((EmployeeException) exception).getCode())
                .isEqualTo(code);
    }
}
