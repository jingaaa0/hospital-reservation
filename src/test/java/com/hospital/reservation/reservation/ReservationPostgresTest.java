package com.hospital.reservation.reservation;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.temporal.TemporalAdjusters;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import javax.sql.DataSource;

import com.hospital.reservation.doctor.Department;
import com.hospital.reservation.doctor.Doctor;
import com.hospital.reservation.doctor.DoctorRepository;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

// 기존 예약 기능의 동시성 검증 TEST
@SpringBootTest
@Testcontainers
@AutoConfigureMockMvc
class ReservationPostgresTest {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private MockMvc mockMvc;

    @Container
    @ServiceConnection
    static PostgreSQLContainer postgres =
            new PostgreSQLContainer("postgres:16-alpine")
                    .withDatabaseName("hospital_reservation_test");

    @Test
    void connectsToPostgres() {
        String version = jdbcTemplate.queryForObject(
                "SELECT version()",
                String.class
        );

        String databaseName = jdbcTemplate.queryForObject(
                "SELECT current_database()",
                String.class
        );

        assertThat(version).contains("PostgreSQL");
        assertThat(databaseName).isEqualTo("hospital_reservation_test");
    }

    @BeforeEach
    void applyReservationIndex() {
        ResourceDatabasePopulator populator =
                new ResourceDatabasePopulator(
                        new FileSystemResource(
                                "scripts/migrate-reservation-slot-uniqueness.sql"
                        )
                );

        populator.execute(dataSource);
    }

    @Test
    void createsUniqueIndexForNonCancelledReservations() {
        String definition = jdbcTemplate.queryForObject(
                """
                SELECT indexdef
                FROM pg_indexes
                WHERE schemaname = 'public'
                AND tablename = 'reservations'
                AND indexname = 'uk_reservations_non_cancelled_schedule'
                """,
                String.class
        );

        assertThat(definition)
                .contains("CREATE UNIQUE INDEX")
                .contains("(doctor_id, appointment_date, appointment_time)")
                .contains("WHERE")
                .contains("<>")
                .contains("'CANCELLED'");
    }

    @Test
    void rejectsDuplicateNonCancelledReservations() {
        Doctor doctor = doctorRepository.saveAndFlush(Doctor.create(
                Department.INTERNAL_MEDICINE,
                "중복 검증 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY),
                1
        ));

        LocalDate date = LocalDate.of(2030, 1, 7);
        LocalTime time = LocalTime.of(10, 0);

        Reservation first = Reservation.create(
                "첫 번째 환자", "01011112222",
                doctor, date, time, null, true
        );
        reservationRepository.saveAndFlush(first);

        Reservation second = Reservation.create(
                "두 번째 환자", "01033334444",
                doctor, date, time, null, true
        );

        assertThatThrownBy(
                () -> reservationRepository.saveAndFlush(second)
        )
                .isInstanceOf(DataIntegrityViolationException.class)
                .hasStackTraceContaining(
                        "uk_reservations_non_cancelled_schedule"
                );

        Long savedCount = jdbcTemplate.queryForObject("""
                SELECT COUNT(*)
                FROM reservations
                WHERE doctor_id = ?
                AND appointment_date = ?
                AND appointment_time = ?
                AND status <> 'CANCELLED'
                """,
                Long.class,
                doctor.getId(), date, time
        );

        assertThat(savedCount).isEqualTo(1L);
    }

    @Test
    void allowsRebookingAfterCancellation() {
        Doctor doctor = doctorRepository.saveAndFlush(Doctor.create(
                Department.INTERNAL_MEDICINE,
                "재예약 검증 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY),
                1
        ));

        LocalDate date = LocalDate.of(2030, 1, 7);
        LocalTime time = LocalTime.of(10, 0);

        // 처음에는 신청 상태로 DB에 저장합니다.
        Reservation original = reservationRepository.saveAndFlush(
                Reservation.create(
                        "기존 환자", "01011112222",
                        doctor, date, time, null, true
                )
        );

        // 저장했던 예약을 취소하고, 변경 내용을 DB에 반영합니다.
        original.changeStatus(ReservationStatus.CANCELLED);
        reservationRepository.saveAndFlush(original);

        // 같은 의사·날짜·시간으로 새 예약을 저장합니다.
        Reservation rebooked = reservationRepository.saveAndFlush(
                Reservation.create(
                        "새 환자", "01033334444",
                        doctor, date, time, null, true
                )
        );

        assertThat(rebooked.getId()).isNotEqualTo(original.getId());

        // 객체의 값만 보지 않고 DB에 실제 저장된 상태를 확인합니다.
        var statuses = jdbcTemplate.queryForList("""
                SELECT status
                FROM reservations
                WHERE doctor_id = ?
                AND appointment_date = ?
                AND appointment_time = ?
                """,
                String.class,
                doctor.getId(), date, time
        );

        assertThat(statuses).containsExactlyInAnyOrder(
                "CANCELLED",
                "REQUESTED"
        );
    }

    @Test
    void concurrentRequestsCreateOnlyOneReservation() throws Exception {
        Doctor doctor = doctorRepository.saveAndFlush(Doctor.create(
                Department.INTERNAL_MEDICINE,
                "동시 요청 검증 의사",
                LocalDate.of(1980, 1, 1),
                Set.of(DayOfWeek.MONDAY),
                1
        ));

        LocalDate date = LocalDate.now()
                .with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        LocalTime time = LocalTime.of(10, 0);

        String requestBody = """
                {
                "name": "테스트 환자",
                "phoneNumber": "01012345678",
                "department": "INTERNAL_MEDICINE",
                "doctorId": %d,
                "date": "%s",
                "appointmentTime": "10:00",
                "symptom": "동시 예약 검증",
                "privacyAgreed": true
                }
                """.formatted(doctor.getId(), date);

        ExecutorService executor = Executors.newFixedThreadPool(2);
        CountDownLatch ready = new CountDownLatch(2);
        CountDownLatch start = new CountDownLatch(1);

        try {
            Callable<MvcResult> task = () -> {
                // 준비 완료를 알린 뒤 출발 신호를 기다립니다.
                ready.countDown();

                if (!start.await(10, TimeUnit.SECONDS)) {
                    throw new IllegalStateException("출발 신호 대기 시간 초과");
                }

                return mockMvc.perform(post("/api/reservations")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(requestBody))
                        .andReturn();
            };

            Future<MvcResult> first = executor.submit(task);
            Future<MvcResult> second = executor.submit(task);

            // 두 작업이 모두 준비됐을 때 출발시킵니다.
            assertThat(ready.await(10, TimeUnit.SECONDS)).isTrue();
            start.countDown();

            MvcResult firstResult = first.get(20, TimeUnit.SECONDS);
            MvcResult secondResult = second.get(20, TimeUnit.SECONDS);

            int firstStatus = firstResult.getResponse().getStatus();
            int secondStatus = secondResult.getResponse().getStatus();

            assertThat(new int[]{firstStatus, secondStatus})
                    .containsExactlyInAnyOrder(201, 409);

            MvcResult conflictResult =
                    firstStatus == 409 ? firstResult : secondResult;

            jsonPath("$.code")
                    .value("RESERVATION_SLOT_ALREADY_TAKEN")
                    .match(conflictResult);

            Long savedCount = jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM reservations
                    WHERE doctor_id = ?
                    AND appointment_date = ?
                    AND appointment_time = ?
                    """,
                    Long.class,
                    doctor.getId(), date, time
            );

            assertThat(savedCount).isEqualTo(1L);
        } finally {
            // 테스트가 실패하더라도 대기 중인 작업을 해제하고 정리합니다.
            start.countDown();
            executor.shutdownNow();
        }
    }
}