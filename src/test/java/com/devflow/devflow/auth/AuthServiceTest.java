package com.devflow.devflow.auth;

import com.devflow.devflow.auth.dto.AuthResponse;
import com.devflow.devflow.auth.dto.LoginRequest;
import com.devflow.devflow.auth.dto.RegisterRequest;
import com.devflow.devflow.common.exception.EmailAlreadyExistsException;
import com.devflow.devflow.common.exception.InvalidCredentialsException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider tokenProvider;

    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthService authService;

    @Test
    void testRegisterSuccess() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password123", Role.ORGANIZATION_ADMIN);
        User savedUser = new User(1L, "test@example.com", "encoded_password", Role.ORGANIZATION_ADMIN, null);

        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(passwordEncoder.encode("password123")).thenReturn("encoded_password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(tokenProvider.generateToken("test@example.com", Role.ORGANIZATION_ADMIN)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("Bearer", response.getTokenType());
        assertEquals("test@example.com", response.getEmail());
        assertEquals(Role.ORGANIZATION_ADMIN, response.getRole());

        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterDuplicateEmailThrowsException() {
        RegisterRequest request = new RegisterRequest("existing@example.com", "password123", Role.DEVELOPER);

        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("user@example.com", "password123");
        User user = new User(1L, "user@example.com", "encoded_password", Role.DEVELOPER, null);

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(tokenProvider.generateToken("user@example.com", Role.DEVELOPER)).thenReturn("mock.jwt.token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("mock.jwt.token", response.getToken());
        assertEquals("user@example.com", response.getEmail());
        assertEquals(Role.DEVELOPER, response.getRole());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }

    @Test
    void testLoginInvalidCredentialsThrowsException() {
        LoginRequest request = new LoginRequest("user@example.com", "wrongpassword");

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        assertThrows(InvalidCredentialsException.class, () -> authService.login(request));
    }
}
