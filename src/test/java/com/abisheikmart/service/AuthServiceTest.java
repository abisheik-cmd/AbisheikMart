package com.abisheikmart.service;

import com.abisheikmart.dao.UserDao;
import com.abisheikmart.dto.LoginRequest;
import com.abisheikmart.dto.RegisterRequest;
import com.abisheikmart.model.User;
import com.abisheikmart.util.PasswordUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

class AuthServiceTest {

    private UserDao userDao;
    private AuthService authService;

    @BeforeEach
    void setUp() {
        userDao = org.mockito.Mockito.mock(UserDao.class);
        authService = new AuthService(userDao);
    }

    @Test
    @DisplayName("Registering new user with valid details should succeed")
    void testRegisterSuccess() throws SQLException {
        RegisterRequest req = new RegisterRequest();
        req.setName("Test Buyer");
        req.setEmail("buyer@example.com");
        req.setPassword("Password123");
        req.setRole("CUSTOMER");

        org.mockito.Mockito.when(userDao.existsByEmail("buyer@example.com")).thenReturn(false);
        org.mockito.Mockito.when(userDao.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(100L);
            return u;
        });

        User user = authService.register(req);

        assertNotNull(user);
        assertEquals(100L, user.getId());
        assertEquals("buyer@example.com", user.getEmail());
        assertEquals("CUSTOMER", user.getRole());
        assertTrue(PasswordUtil.checkPassword("Password123", user.getPasswordHash()));
    }

    @Test
    @DisplayName("Registering with existing email should throw IllegalArgumentException")
    void testRegisterDuplicateEmail() {
        RegisterRequest req = new RegisterRequest();
        req.setEmail("existing@example.com");
        req.setPassword("Password123");

        org.mockito.Mockito.when(userDao.existsByEmail("existing@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> authService.register(req));
    }

    @Test
    @DisplayName("Logging in with correct credentials should succeed")
    void testLoginSuccess() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("CorrectPass123");

        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setEmail("user@example.com");
        existingUser.setPasswordHash(PasswordUtil.hashPassword("CorrectPass123"));
        existingUser.setRole("CUSTOMER");

        org.mockito.Mockito.when(userDao.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        User loggedIn = authService.login(req);

        assertNotNull(loggedIn);
        assertEquals(1L, loggedIn.getId());
    }

    @Test
    @DisplayName("Logging in with incorrect password should throw IllegalArgumentException")
    void testLoginInvalidPassword() {
        LoginRequest req = new LoginRequest();
        req.setEmail("user@example.com");
        req.setPassword("WrongPass123");

        User existingUser = new User();
        existingUser.setEmail("user@example.com");
        existingUser.setPasswordHash(PasswordUtil.hashPassword("CorrectPass123"));

        org.mockito.Mockito.when(userDao.findByEmail("user@example.com")).thenReturn(Optional.of(existingUser));

        assertThrows(IllegalArgumentException.class, () -> authService.login(req));
    }
}

