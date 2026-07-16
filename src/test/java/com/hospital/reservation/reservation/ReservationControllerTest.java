package com.hospital.reservation.reservation;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReservationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ReservationRepository reservationRepository;

    @Test
    void publicReservationRequestDoesNotRequireCsrfToken() throws Exception {
        LocalDate nextMonday = LocalDate.now().with(TemporalAdjusters.next(DayOfWeek.MONDAY));
        long countBeforeRequest = reservationRepository.count();

        mockMvc.perform(post("/api/reservations")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(("""
                                {
                                  "name": "홍길동",
                                  "phoneNumber": "01012345678",
                                  "department": "ENT",
                                  "doctorId": 1,
                                  "date": "%s",
                                  "appointmentTime": "10:30",
                                  "symptom": "두통이 있습니다.",
                                  "privacyAgreed": true
                                }
                                """).formatted(nextMonday)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.reservationNumber").isNotEmpty())
                .andExpect(jsonPath("$.message").exists());

        org.assertj.core.api.Assertions.assertThat(reservationRepository.count()).isEqualTo(countBeforeRequest + 1);
    }
}
