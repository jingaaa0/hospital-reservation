package com.hospital.reservation.doctor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class DoctorRepositoryTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Test
    void savesDoctorWithAvailableDays() {
        Doctor doctor = Doctor.create(
                Department.ENT,
                "김도윤",
                LocalDate.of(1980, 3, 12),
                Set.of(DayOfWeek.MONDAY, DayOfWeek.WEDNESDAY, DayOfWeek.FRIDAY),
                1
        );

        Doctor savedDoctor = doctorRepository.saveAndFlush(doctor);
        Doctor foundDoctor = doctorRepository.findById(savedDoctor.getId()).orElseThrow();

        assertThat(foundDoctor.getDepartment()).isEqualTo(Department.ENT);
        assertThat(foundDoctor.getAvailableDays()).containsExactlyInAnyOrder(
                DayOfWeek.MONDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.FRIDAY
        );
        assertThat(foundDoctor.isActive()).isTrue();
        assertThat(foundDoctor.getCreatedAt()).isNotNull();
        assertThat(foundDoctor.getUpdatedAt()).isNotNull();
    }
}
