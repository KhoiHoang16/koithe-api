package com.milktea.report.service.impl;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReportIntegrationTest {

    @Autowired
    MockMvc mockMvc;

    @Test
    @WithMockUser(roles = "CASHIER")
    @DisplayName("CASHIER gọi /api/reports -> 403 Forbidden (không có quyền)")
    void testAsCashier_forbidden() throws Exception {
        mockMvc.perform(get("/api/reports?top=5"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value("Bạn không có quyền truy cập tính năng này"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    @DisplayName("ADMIN gọi /api/reports -> 200 OK thành công")
    void testAsAdmin_success() throws Exception {
        mockMvc.perform(get("/api/reports?top=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @WithMockUser(roles = "MANAGER")
    @DisplayName("MANAGER gọi /api/reports -> 200 OK thành công")
    void testAsManager_success() throws Exception {
        mockMvc.perform(get("/api/reports?top=5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
