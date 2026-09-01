package com.freelance.os.auth;

import com.freelance.os.auth.dto.AuthResponse;
import com.freelance.os.auth.dto.LoginRequest;
import com.freelance.os.auth.dto.RefreshTokenRequest;
import com.freelance.os.auth.dto.RegisterRequest;
import com.freelance.os.user.Role;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void registerUser_Success() {
        RegisterRequest request = RegisterRequest.builder()
                .email("test.freelancer@example.com")
                .password("SecurePass123!")
                .fullName("Jane Doe")
                .role(Role.ROLE_FREELANCER)
                .build();

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("test.freelancer@example.com", response.getUser().getEmail());
        assertEquals(Role.ROLE_FREELANCER, response.getUser().getRole());
    }

    @Test
    void registerUser_DuplicateEmail_ThrowsException() {
        RegisterRequest request = RegisterRequest.builder()
                .email("duplicate@example.com")
                .password("SecurePass123!")
                .fullName("Jane Doe")
                .build();

        authService.register(request);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.register(request));
        assertEquals("Email is already registered", exception.getMessage());
    }

    @Test
    void loginUser_Success() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("login.test@example.com")
                .password("MyPassword123")
                .fullName("John Developer")
                .build();
        authService.register(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("login.test@example.com")
                .password("MyPassword123")
                .build();

        AuthResponse loginResponse = authService.login(loginRequest);

        assertNotNull(loginResponse);
        assertNotNull(loginResponse.getAccessToken());
        assertNotNull(loginResponse.getRefreshToken());
        assertEquals("login.test@example.com", loginResponse.getUser().getEmail());
    }

    @Test
    void loginUser_InvalidPassword_ThrowsException() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("wrong.pass@example.com")
                .password("CorrectPass123")
                .fullName("John Developer")
                .build();
        authService.register(registerRequest);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("wrong.pass@example.com")
                .password("WrongPass123")
                .build();

        RuntimeException exception = assertThrows(RuntimeException.class, () -> authService.login(loginRequest));
        assertEquals("Invalid email or password", exception.getMessage());
    }

    @Test
    void refreshToken_WithRotation_Success() {
        RegisterRequest registerRequest = RegisterRequest.builder()
                .email("token.refresh@example.com")
                .password("MyPassword123")
                .fullName("Refresher Doe")
                .build();
        AuthResponse registerResponse = authService.register(registerRequest);

        String oldRefreshToken = registerResponse.getRefreshToken();
        RefreshTokenRequest refreshRequest = RefreshTokenRequest.builder()
                .refreshToken(oldRefreshToken)
                .build();

        AuthResponse refreshResponse = authService.refreshToken(refreshRequest);

        assertNotNull(refreshResponse);
        assertNotNull(refreshResponse.getAccessToken());
        assertNotNull(refreshResponse.getRefreshToken());
        assertNotEquals(oldRefreshToken, refreshResponse.getRefreshToken(), "Refresh token must be rotated");
        assertEquals("token.refresh@example.com", refreshResponse.getUser().getEmail());

        // Old refresh token must be invalidated
        assertThrows(RuntimeException.class, () -> authService.refreshToken(refreshRequest));
    }
}
