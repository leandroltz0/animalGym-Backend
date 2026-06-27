package com.animalgym.api.service;

import com.animalgym.api.dto.request.LoginRequest;
import com.animalgym.api.dto.response.LoginResponse;
import com.animalgym.api.entity.User;
import com.animalgym.api.exception.UnauthorizedException;
import com.animalgym.api.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    private BCryptPasswordEncoder passwordEncoder;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        passwordEncoder = new BCryptPasswordEncoder();
        authService = new AuthService(userRepository, jwtService, passwordEncoder);
    }

    @Test
    void shouldLoginSuccessfully() {
        String rawPassword = "secret123";
        String hashedPassword = passwordEncoder.encode(rawPassword);
        User user = User.builder()
                .id(1L)
                .email("admin@animalgym.com")
                .passwordHash(hashedPassword)
                .role("ADMIN")
                .build();

        when(userRepository.findByEmail("admin@animalgym.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken("admin@animalgym.com", "ADMIN")).thenReturn("mock-token");

        LoginRequest request = new LoginRequest("admin@animalgym.com", rawPassword);
        LoginResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock-token", response.getToken());
        assertEquals("ADMIN", response.getRole());
        assertEquals("admin@animalgym.com", response.getEmail());
    }

    @Test
    void shouldThrowWhenEmailNotFound() {
        when(userRepository.findByEmail("unknown@test.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("unknown@test.com", "password");

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void shouldThrowWhenPasswordIsWrong() {
        String hashedPassword = passwordEncoder.encode("correct-password");
        User user = User.builder()
                .id(1L)
                .email("admin@animalgym.com")
                .passwordHash(hashedPassword)
                .role("ADMIN")
                .build();

        when(userRepository.findByEmail("admin@animalgym.com")).thenReturn(Optional.of(user));

        LoginRequest request = new LoginRequest("admin@animalgym.com", "wrong-password");

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void shouldNotGenerateTokenOnFailedLogin() {
        when(userRepository.findByEmail("admin@animalgym.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("admin@animalgym.com", "any-password");

        assertThrows(UnauthorizedException.class, () -> authService.login(request));
        verify(jwtService, never()).generateToken(any(), any());
    }
}
