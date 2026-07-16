package com.hospital.reservation.doctor;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Component
public class DoctorBootstrapConfig implements ApplicationRunner {

    private static final Set<DayOfWeek> WEEKDAYS = EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.FRIDAY);
    private static final Set<DayOfWeek> MONDAY_TO_SATURDAY = EnumSet.range(DayOfWeek.MONDAY, DayOfWeek.SATURDAY);

    private final DoctorRepository doctorRepository;

    public DoctorBootstrapConfig(DoctorRepository doctorRepository) {
        this.doctorRepository = doctorRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (doctorRepository.count() > 0) {
            return;
        }

        doctorRepository.saveAll(List.of(
                doctor(Department.ENT, "김도윤 원장", 1978, 3, 12, WEEKDAYS, 1),
                doctor(Department.ENT, "이서현 원장", 1983, 7, 25, MONDAY_TO_SATURDAY, 2),
                doctor(Department.ENT, "박준호 원장", 1980, 11, 8, WEEKDAYS, 3),
                doctor(Department.INTERNAL_MEDICINE, "최은지 원장", 1976, 5, 19, MONDAY_TO_SATURDAY, 1),
                doctor(Department.INTERNAL_MEDICINE, "정민재 원장", 1981, 9, 2, WEEKDAYS, 2),
                doctor(Department.INTERNAL_MEDICINE, "한수빈 원장", 1985, 1, 14, MONDAY_TO_SATURDAY, 3),
                doctor(Department.INTERNAL_MEDICINE, "윤지호 원장", 1979, 12, 6, WEEKDAYS, 4),
                doctor(Department.ORTHOPEDICS, "강현우 원장", 1975, 4, 21, MONDAY_TO_SATURDAY, 1),
                doctor(Department.ORTHOPEDICS, "송예린 원장", 1984, 8, 17, WEEKDAYS, 2),
                doctor(Department.ORTHOPEDICS, "조성민 원장", 1982, 2, 10, MONDAY_TO_SATURDAY, 3),
                doctor(Department.GENERAL_SURGERY, "오세진 원장", 1977, 6, 28, WEEKDAYS, 1),
                doctor(Department.GENERAL_SURGERY, "배지훈 원장", 1986, 10, 3, MONDAY_TO_SATURDAY, 2),
                doctor(Department.NEUROSURGERY, "임태윤 원장", 1974, 1, 30, WEEKDAYS, 1),
                doctor(Department.NEUROSURGERY, "문하린 원장", 1982, 5, 11, MONDAY_TO_SATURDAY, 2),
                doctor(Department.REHABILITATION_MEDICINE, "서재원 원장", 1979, 7, 7, WEEKDAYS, 1),
                doctor(Department.REHABILITATION_MEDICINE, "남유진 원장", 1987, 3, 23, MONDAY_TO_SATURDAY, 2),
                doctor(Department.REHABILITATION_MEDICINE, "권민석 원장", 1983, 11, 16, WEEKDAYS, 3)
        ));
    }

    private Doctor doctor(
            Department department,
            String name,
            int year,
            int month,
            int day,
            Set<DayOfWeek> availableDays,
            int displayOrder
    ) {
        return Doctor.create(department, name, LocalDate.of(year, month, day), availableDays, displayOrder);
    }
}
