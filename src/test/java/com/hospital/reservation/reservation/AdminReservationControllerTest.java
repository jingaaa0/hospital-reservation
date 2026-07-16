package com.hospital.reservation.reservation;

import com.hospital.reservation.doctor.Doctor;
import com.hospital.reservation.doctor.DoctorRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AdminReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepository reservationRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private EntityManager entityManager;

    @Test
    @WithMockUser(roles = "ADMIN")
    void filtersSortsAndUpdatesReservation() throws Exception {
        Doctor doctor = doctorRepository.findById(1L).orElseThrow();
        LocalDate appointmentDate = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        Reservation reservation = reservationRepository.saveAndFlush(Reservation.create(
                "관리자조회테스트",
                "01099998888",
                doctor,
                appointmentDate,
                LocalTime.of(15, 0),
                "관리자 예약 조회 테스트 증상",
                true
        ));

        mockMvc.perform(get("/api/admin/reservations")
                        .param("department", "ENT")
                        .param("doctorId", doctor.getId().toString())
                        .param("date", appointmentDate.toString())
                        .param("status", "REQUESTED")
                        .param("sortBy", "doctor")
                        .param("direction", "DESC"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.items[0].patientName").value("관리자조회테스트"))
                .andExpect(jsonPath("$.items[0].departmentName").value("이비인후과"));

        mockMvc.perform(patch("/api/admin/reservations/{reservationId}/status", reservation.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"CONFIRMED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CONFIRMED"));

        mockMvc.perform(patch("/api/admin/reservations/{reservationId}/memo", reservation.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"adminMemo\":\"전화 상담 후 예약 확정\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.adminMemo").value("전화 상담 후 예약 확정"));

        entityManager.clear();
        assertThat(reservationRepository.findById(reservation.getId()).orElseThrow().getAdminMemo())
                .isEqualTo("전화 상담 후 예약 확정");
    }
}
