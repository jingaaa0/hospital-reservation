package com.hospital.reservation.employee;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
public class EmployeeController {

    private final EmployeeService employeeService;

    public EmployeeController(EmployeeService employeeService) {
        this.employeeService = employeeService;
    }

    @GetMapping("/api/admin/employees")
    public List<EmployeeResponse> findAll(
            @RequestParam(defaultValue = "ALL") EmployeeType type
    ) {
        return employeeService.findAll(type);
    }

    @PostMapping("/api/admin/employees")
    @ResponseStatus(HttpStatus.CREATED)
    public EmployeeResponse create(@Valid @RequestBody EmployeeSaveRequest request) {
        return employeeService.create(request);
    }

    @PutMapping("/api/admin/employees/{type}/{employeeId}")
    public EmployeeResponse update(
            @PathVariable EmployeeType type,
            @PathVariable Long employeeId,
            @Valid @RequestBody EmployeeSaveRequest request
    ) {
        return employeeService.update(type, employeeId, request);
    }

    @DeleteMapping("/api/admin/employees/{type}/{employeeId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable EmployeeType type, @PathVariable Long employeeId) {
        employeeService.delete(type, employeeId);
    }
}
