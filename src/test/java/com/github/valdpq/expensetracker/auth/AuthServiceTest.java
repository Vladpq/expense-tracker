package com.github.valdpq.expensetracker.auth;

import com.github.valdpq.expensetracker.auth.dto.AuthResponse;
import com.github.valdpq.expensetracker.auth.dto.LoginRequest;
import com.github.valdpq.expensetracker.auth.dto.RegisterRequest;
import com.github.valdpq.expensetracker.user.User;
import com.github.valdpq.expensetracker.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;


import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    @Test
    void register_shouldThrowEmailAlreadyExistsException_whenEmailAlreadyRegistered() {
        RegisterRequest registerRequest = new RegisterRequest("email@test.com", "password");

        when(userRepository.existsByEmail(anyString()))
                .thenReturn(true);

        assertThrows(EmailAlreadyExistsException.class, () -> authService.register(registerRequest));
    }

    @Test
    void register_shouldSaveEncodedPassword_whenUserIsSaved() {
        RegisterRequest registerRequest = new RegisterRequest("email@test.com", "password");

        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedPassword");
        when(jwtService.generateToken(any()))
                .thenReturn("token");

        authService.register(registerRequest);

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository).save(captor.capture());
        assertEquals("encodedPassword", captor.getValue().getPassword());
    }

    @Test
    void register_shouldReturnToken_whenUserIsRegistered() {
        RegisterRequest registerRequest = new RegisterRequest("email@test.com", "password");

        when(userRepository.existsByEmail(anyString()))
                .thenReturn(false);
        when(passwordEncoder.encode(anyString()))
                .thenReturn("encodedPassword");
        when(jwtService.generateToken(any()))
                .thenReturn("token");

        AuthResponse response = authService.register(registerRequest);
        assertNotNull(response);
        assertEquals("token", response.token());
    }

    @Test
    void login_shouldReturnToken_whenUserIsLoggedIn() {
        LoginRequest loginRequest = new LoginRequest("email@test.com", "password");

        Authentication authentication = mock(Authentication.class);
        UserDetails userDetails = mock(UserDetails.class);

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenReturn(authentication);
        when(authentication.getPrincipal())
                .thenReturn(userDetails);
        when(jwtService.generateToken(userDetails))
                .thenReturn("token");

        AuthResponse response = authService.login(loginRequest);
        assertNotNull(response);
        assertEquals("token", response.token());
    }

    @Test
    public void login_shouldThrowBadCredentialsException_whenInvalidEmailOrPassword() {
        LoginRequest loginRequest = new LoginRequest("email@test.com", "password");

        when(authenticationManager.authenticate(any()))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThrows(BadCredentialsException.class, () -> authService.login(loginRequest));
    }
}
