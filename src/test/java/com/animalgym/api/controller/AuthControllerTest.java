package com.animalgym.api.controller;

import com.animalgym.api.dto.request.LoginRequest;
import com.animalgym.api.dto.response.LoginResponse;
import com.animalgym.api.exception.UnauthorizedException;
import com.animalgym.api.service.AuthService;
import com.animalgym.api.service.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@Import({com.animalgym.api.config.SecurityConfig.class, com.animalgym.api.config.JwtAuthFilter.class})
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @MockBean
    private JwtService jwtService;

    @Test
    void shouldReturnTokenOnSuccessfulLogin() throws Exception {
        LoginResponse loginResponse = LoginResponse.builder()
                .token("eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBh...")
                .role("ADMIN")
                .email("admin@animalgym.com")
                .build();

        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse);

        LoginRequest request = new LoginRequest("admin@animalgym.com", "secret");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value(loginResponse.getToken()))
                .andExpect(jsonPath("$.role").value("ADMIN"))
                .andExpect(jsonPath("$.email").value("admin@animalgym.com"));
    }

    @Test
    void shouldReturn401WhenCredentialsAreInvalid() throws Exception {
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new UnauthorizedException("Invalid email or password"));

        LoginRequest request = new LoginRequest("wrong@test.com", "wrong");

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void shouldReturn400WhenEmailIsMissing() throws Exception {
        String invalidJson = "{\"password\":\"secret\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void shouldReturn400WhenPasswordIsMissing() throws Exception {
        String invalidJson = "{\"email\":\"admin@test.com\"}";

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidJson))
                .andExpect(status().isBadRequest());
    }
}
