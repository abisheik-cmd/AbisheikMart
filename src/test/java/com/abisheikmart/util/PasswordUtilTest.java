package com.abisheikmart.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PasswordUtilTest {

    @Test
    @DisplayName("Password hashing should produce valid BCrypt hash and verify correctly")
    void testHashAndPasswordVerification() {
        String plainPassword = "SecretPassword123";
        String hashed = PasswordUtil.hashPassword(plainPassword);

        assertNotNull(hashed);
        assertTrue(hashed.startsWith("$2a$") || hashed.startsWith("$2b$"));
        assertTrue(PasswordUtil.checkPassword(plainPassword, hashed));
        assertFalse(PasswordUtil.checkPassword("WrongPassword", hashed));
    }

    @Test
    @DisplayName("Checking null or empty passwords should return false")
    void testNullOrEmptyPasswordChecks() {
        String hashed = PasswordUtil.hashPassword("ValidPass123");

        assertFalse(PasswordUtil.checkPassword(null, hashed));
        assertFalse(PasswordUtil.checkPassword("", hashed));
        assertFalse(PasswordUtil.checkPassword("ValidPass123", null));
        assertFalse(PasswordUtil.checkPassword("ValidPass123", ""));
    }
}

