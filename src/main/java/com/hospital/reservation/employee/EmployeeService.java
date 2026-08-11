package com.hospital.reservation.employee;

import com.hospital.reservation.doctor.DoctorRepository;
import com.hospital.reservation.doctor.Department;
import com.hospital.reservation.doctor.Doctor;
import com.hospital.reservation.doctor.DoctorEmploymentStatus;
import com.hospital.reservation.reservation.ReservationRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class EmployeeService {
    private final DoctorRepository doctorRepository;
    private final EmployeeRepository employeeRepository;
    private final ReservationRepository reservationRepository;

    public EmployeeService(
            DoctorRepository doctorRepository,
            EmployeeRepository employeeRepository,
            ReservationRepository reservationRepository
    ) {
        this.doctorRepository = doctorRepository;
        this.employeeRepository = employeeRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional(readOnly = true)
    public List<EmployeeResponse> findAll(EmployeeType type) {
        if (type == EmployeeType.DOCTOR) {
            return findDoctors();
        }
        if (type == EmployeeType.STAFF) {
            return findStaff();
        }

        List<EmployeeResponse> employees = new ArrayList<>();
        employees.addAll(findDoctors());
        employees.addAll(findStaff());
        return employees;
    }

    @Transactional
    public EmployeeResponse create(EmployeeSaveRequest request) {
        validateEditableType(request.type());
        if (request.type() == EmployeeType.DOCTOR) {
            requireAvailableDays(request);
            Department department = enumValue(Department.class, request.department(), "진료과를 확인해 주세요.");
            if (doctorRepository.existsByDepartmentAndName(department, request.name().trim())) {
                throw conflict();
            }
            Doctor doctor = Doctor.create(
                    department,
                    request.name().trim(),
                    request.birthDate(),
                    request.availableDays(),
                    request.displayOrder()
            );
            doctor.changeEmploymentStatus(status(request.employmentStatus()));
            return EmployeeResponse.from(doctorRepository.save(doctor));
        }

        EmployeeDepartment department = enumValue(
                EmployeeDepartment.class, request.department(), "부서를 확인해 주세요."
        );
        requirePosition(request.position());
        if (employeeRepository.existsByDepartmentAndName(department, request.name().trim())) {
            throw conflict();
        }
        Employee employee = Employee.create(
                department,
                request.name().trim(),
                request.position().trim(),
                request.birthDate(),
                request.displayOrder()
        );
        employee.changeEmploymentStatus(employeeStatus(request.employmentStatus()));
        return EmployeeResponse.from(employeeRepository.save(employee));
    }

    @Transactional
    public EmployeeResponse update(EmployeeType type, Long employeeId, EmployeeSaveRequest request) {
        validateEditableType(type);
        if (type != request.type()) {
            throw new EmployeeException("TYPE_MISMATCH", "직원 구분은 수정할 수 없습니다.", HttpStatus.BAD_REQUEST);
        }
        if (type == EmployeeType.DOCTOR) {
            requireAvailableDays(request);
            Doctor doctor = findDoctor(employeeId);
            Department department = enumValue(Department.class, request.department(), "진료과를 확인해 주세요.");
            if (doctorRepository.existsByDepartmentAndNameAndIdNot(department, request.name().trim(), employeeId)) {
                throw conflict();
            }
            doctor.update(department, request.name(), request.birthDate(), request.availableDays(),
                    status(request.employmentStatus()), request.displayOrder());
            return EmployeeResponse.from(doctor);
        }

        Employee employee = findEmployee(employeeId);
        EmployeeDepartment department = enumValue(
                EmployeeDepartment.class, request.department(), "부서를 확인해 주세요."
        );
        requirePosition(request.position());
        if (employeeRepository.existsByDepartmentAndNameAndIdNot(department, request.name().trim(), employeeId)) {
            throw conflict();
        }
        employee.update(department, request.name(), request.position(), request.birthDate(),
                employeeStatus(request.employmentStatus()), request.displayOrder());
        return EmployeeResponse.from(employee);
    }

    @Transactional
    public void delete(EmployeeType type, Long employeeId) {
        validateEditableType(type);
        if (type == EmployeeType.DOCTOR) {
            Doctor doctor = findDoctor(employeeId);
            if (reservationRepository.existsByDoctorId(employeeId)) {
                throw new EmployeeException(
                        "DOCTOR_HAS_RESERVATIONS",
                        "예약 이력이 있는 의사는 삭제할 수 없습니다. 퇴사 상태로 변경해 주세요.",
                        HttpStatus.CONFLICT
                );
            }
            doctorRepository.delete(doctor);
            return;
        }
        employeeRepository.delete(findEmployee(employeeId));
    }

    private List<EmployeeResponse> findDoctors() {
        return doctorRepository.findAllByOrderByDepartmentAscDisplayOrderAscNameAsc()
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    private List<EmployeeResponse> findStaff() {
        return employeeRepository.findAllByOrderByDepartmentAscDisplayOrderAscNameAsc()
                .stream()
                .map(EmployeeResponse::from)
                .toList();
    }

    private Doctor findDoctor(Long employeeId) {
        return doctorRepository.findById(employeeId)
                .orElseThrow(() -> notFound());
    }

    private Employee findEmployee(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> notFound());
    }

    private void validateEditableType(EmployeeType type) {
        if (type == null || type == EmployeeType.ALL) {
            throw new EmployeeException("INVALID_EMPLOYEE_TYPE", "직원 구분을 확인해 주세요.", HttpStatus.BAD_REQUEST);
        }
    }

    private void requirePosition(String position) {
        if (position == null || position.isBlank()) {
            throw new EmployeeException("POSITION_REQUIRED", "직함을 입력해 주세요.", HttpStatus.BAD_REQUEST);
        }
    }

    private void requireAvailableDays(EmployeeSaveRequest request) {
        if (request.availableDays() == null || request.availableDays().isEmpty()) {
            throw new EmployeeException(
                    "AVAILABLE_DAYS_REQUIRED",
                    "진료 가능 요일을 한 개 이상 선택해 주세요.",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private DoctorEmploymentStatus status(String value) {
        return enumValue(DoctorEmploymentStatus.class, value, "재직 상태를 확인해 주세요.");
    }

    private EmployeeEmploymentStatus employeeStatus(String value) {
        return enumValue(EmployeeEmploymentStatus.class, value, "재직 상태를 확인해 주세요.");
    }

    private <T extends Enum<T>> T enumValue(Class<T> enumClass, String value, String message) {
        try {
            return Enum.valueOf(enumClass, value);
        } catch (IllegalArgumentException | NullPointerException exception) {
            throw new EmployeeException("INVALID_VALUE", message, HttpStatus.BAD_REQUEST);
        }
    }

    private EmployeeException notFound() {
        return new EmployeeException("EMPLOYEE_NOT_FOUND", "직원 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND);
    }

    private EmployeeException conflict() {
        return new EmployeeException("DUPLICATE_EMPLOYEE", "같은 부서에 동일한 이름의 직원이 있습니다.", HttpStatus.CONFLICT);
    }
}
