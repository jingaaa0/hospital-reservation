package com.hospital.reservation.doctor;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void returnsDepartmentsFromDatabase() throws Exception {
        mockMvc.perform(get("/api/departments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("ENT"))
                .andExpect(jsonPath("$[0].name").value("이비인후과"))
                .andExpect(jsonPath("$.length()").value(6));
    }

    @Test
    void returnsDoctorsForSelectedDepartment() throws Exception {
        mockMvc.perform(get("/api/doctors").param("department", "GENERAL_SURGERY"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("오세진"))
                .andExpect(jsonPath("$[0].doctorId").isNumber());
    }
}
