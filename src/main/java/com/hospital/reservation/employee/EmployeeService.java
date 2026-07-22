package com.hospital.reservation.employee;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class EmployeeService {
    @Transactional
    public EmployeeResponse create(EmployeeRequest request) {

        return new EmployeeResponse(
                null,
                "직원 저장 로직은 아직 구현되지 않았습니다."
        );
    }
}
