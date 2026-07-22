package com.hospital.reservation.doctor;

import jakarta.persistence.EntityManager;
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

    @Autowired
    private EntityManager entityManager;

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
        assertThat(foundDoctor.getEmploymentStatus()).isEqualTo(DoctorEmploymentStatus.Y);
        assertThat(foundDoctor.getCreatedAt()).isNotNull();
        assertThat(foundDoctor.getUpdatedAt()).isNotNull();
    }

    @Test
    void storesEmploymentStatusAsSingleCharacterAndFiltersOnlyEmployedDoctors() {
        Doctor doctor = Doctor.create(
                Department.ENT,
                "휴직 의사",
                LocalDate.of(1985, 5, 10),
                Set.of(DayOfWeek.TUESDAY),
                2
        );
        doctor.changeEmploymentStatus(DoctorEmploymentStatus.L);
        Doctor savedDoctor = doctorRepository.saveAndFlush(doctor);

        String storedStatus = (String) entityManager.createNativeQuery(
                        "select active from doctors where id = :id",
                        String.class
                )
                .setParameter("id", savedDoctor.getId())
                .getSingleResult();

        assertThat(storedStatus).isEqualTo("L");
        assertThat(doctorRepository.findAllByEmploymentStatusOrderByDepartmentAscDisplayOrderAscNameAsc(
                DoctorEmploymentStatus.Y
        )).doesNotContain(savedDoctor);
    }
}
