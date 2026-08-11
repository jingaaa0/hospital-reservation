package com.hospital.reservation.employee;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "admin.username=test-admin",
        "admin.password=test-password"
})
@AutoConfigureMockMvc
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EmployeeRepository employeeRepository;

    @Test
    void unauthenticatedUserCannotReadEmployeeList() throws Exception {
        mockMvc.perform(get("/api/admin/employees"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void readsOnlyDoctorsWhenDoctorFilterIsSelected() throws Exception {
        mockMvc.perform(get("/api/admin/employees").param("type", "DOCTOR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("DOCTOR"))
                .andExpect(jsonPath("$[0].department").isString())
                .andExpect(jsonPath("$[0].employmentStatus").value("Y"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void readsOnlyGeneralStaffWhenStaffFilterIsSelected() throws Exception {
        mockMvc.perform(get("/api/admin/employees").param("type", "STAFF"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].type").value("STAFF"))
                .andExpect(jsonPath("$[0].position").isString());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void createsUpdatesAndDeletesGeneralStaff() throws Exception {
        String createBody = """
                {
                  "type": "STAFF",
                  "name": "테스트직원",
                  "department": "ADMINISTRATION",
                  "position": "접수 담당",
                  "birthDate": "1993-04-15",
                  "employmentStatus": "Y",
                  "displayOrder": 99,
                  "availableDays": []
                }
                """;

        mockMvc.perform(post("/api/admin/employees")
                        .with(csrf())
                        .contentType("application/json")
                        .content(createBody))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("테스트직원"));

        long employeeId = employeeRepository
                .findAllByOrderByDepartmentAscDisplayOrderAscNameAsc()
                .stream()
                .filter(employee -> employee.getName().equals("테스트직원"))
                .findFirst()
                .orElseThrow()
                .getId();
        String updateBody = createBody.replace("접수 담당", "원무 담당").replace("\"Y\"", "\"L\"");

        mockMvc.perform(put("/api/admin/employees/STAFF/{employeeId}", employeeId)
                        .with(csrf())
                        .contentType("application/json")
                        .content(updateBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.position").value("원무 담당"))
                .andExpect(jsonPath("$.employmentStatus").value("L"));

        mockMvc.perform(delete("/api/admin/employees/STAFF/{employeeId}", employeeId).with(csrf()))
                .andExpect(status().isNoContent());
    }
}
