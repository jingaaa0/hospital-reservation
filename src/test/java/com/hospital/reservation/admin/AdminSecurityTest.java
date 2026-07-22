package com.hospital.reservation.admin;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.security.test.context.support.WithMockUser;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestBuilders.formLogin;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = {
        "admin.username=test-admin",
        "admin.password=test-password"
})
@AutoConfigureMockMvc
class AdminSecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void unauthenticatedAdminRequestRedirectsToLogin() throws Exception {
        mockMvc.perform(get("/admin/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login"));
    }

    @Test
    void configuredAdminCanLogin() throws Exception {
        mockMvc.perform(formLogin("/admin/login")
                        .user("test-admin")
                        .password("test-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/"));
    }

    @Test
    void invalidPasswordReturnsToLoginWithError() throws Exception {
        mockMvc.perform(formLogin("/admin/login")
                        .user("test-admin")
                        .password("wrong-password"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/login?error"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rendersInquiryPageWithSharedSidebar() throws Exception {
        mockMvc.perform(get("/admin/"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("1:1 문의 목록")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("nav-item active")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rendersReservationPageWithSharedSidebar() throws Exception {
        mockMvc.perform(get("/admin/reservations"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("진료 예약 목록")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("nav-item active")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void rendersEmployeePageWithTypeFilters() throws Exception {
        mockMvc.perform(get("/admin/employees"))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("직원 목록")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-employee-filter=\"DOCTOR\"")))
                .andExpect(content().string(org.hamcrest.Matchers.containsString("data-employee-filter=\"STAFF\"")));
    }
}
