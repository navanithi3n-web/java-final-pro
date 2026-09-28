package com.gym.tracker.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gym.tracker.dto.LoginRequest;
import com.gym.tracker.entity.AppUser;
import com.gym.tracker.repository.AppUserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class SecurityTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    public void setup() {
        if (!userRepository.existsByUsername("admin")) {
            userRepository.save(new AppUser("admin", passwordEncoder.encode("admin123"), "Administrator", AppUser.Role.ADMIN));
        }
        if (!userRepository.existsByUsername("staff")) {
            userRepository.save(new AppUser("staff", passwordEncoder.encode("staff123"), "Front Desk Staff", AppUser.Role.STAFF));
        }
    }

    @Test
    @DisplayName("Unauthenticated API request -> 401 Unauthorized")
    public void testUnauthenticatedApiAccess() throws Exception {
        mockMvc.perform(get("/api/members"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @DisplayName("Unauthenticated page request -> 302 Redirect to /login")
    public void testUnauthenticatedPageAccess() throws Exception {
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("Login with valid admin credentials -> 200 OK with UserResponse (no password)")
    public void testValidLogin() throws Exception {
        LoginRequest req = new LoginRequest("admin", "admin123");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    @DisplayName("Login with invalid credentials -> 401 Unauthorized")
    public void testInvalidLogin() throws Exception {
        LoginRequest req = new LoginRequest("admin", "wrongpassword");
        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value(containsString("Invalid username or password")));
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("STAFF role cannot access /api/users -> 403 Forbidden")
    public void testStaffCannotAccessUsersApi() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "staff", roles = {"STAFF"})
    @DisplayName("STAFF role cannot perform DELETE on /api/members/1 -> 403 Forbidden")
    public void testStaffCannotDeleteMember() throws Exception {
        mockMvc.perform(delete("/api/members/1"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    @DisplayName("ADMIN role can access /api/users -> 200 OK")
    public void testAdminCanAccessUsersApi() throws Exception {
        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }
}
