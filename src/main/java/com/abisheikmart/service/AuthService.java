package com.abisheikmart.service;

import com.abisheikmart.dao.UserDao;
import com.abisheikmart.dto.LoginRequest;
import com.abisheikmart.dto.RegisterRequest;
import com.abisheikmart.model.User;
import com.abisheikmart.util.PasswordUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;
import java.util.Optional;

public class AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    private final UserDao userDao;

    public AuthService() {
        this.userDao = new UserDao();
    }

    public AuthService(UserDao userDao) {
        this.userDao = userDao;
    }

    public User register(RegisterRequest request) throws IllegalArgumentException, SQLException {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email address is required.");
        }
        if (request.getPassword() == null || request.getPassword().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long.");
        }
        if (userDao.existsByEmail(request.getEmail().trim())) {
            throw new IllegalArgumentException("An account with this email address already exists.");
        }

        String role = ("SELLER".equalsIgnoreCase(request.getRole())) ? "SELLER" : "CUSTOMER";
        String hashed = PasswordUtil.hashPassword(request.getPassword());

        User user = new User();
        user.setName(request.getName() != null && !request.getName().isBlank() ? request.getName().trim() : request.getEmail().split("@")[0]);
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPasswordHash(hashed);
        user.setRole(role);

        User saved = userDao.save(user);
        logger.info("Successfully registered user id: {}, email: {}, role: {}", saved.getId(), saved.getEmail(), saved.getRole());
        return saved;
    }

    public User login(LoginRequest request) throws IllegalArgumentException {
        if (request.getEmail() == null || request.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email address is required.");
        }
        if (request.getPassword() == null || request.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required.");
        }

        Optional<User> userOpt = userDao.findByEmail(request.getEmail().trim().toLowerCase());
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password.");
        }

        User user = userOpt.get();
        if (!PasswordUtil.checkPassword(request.getPassword(), user.getPasswordHash())) {
            logger.warn("Failed login attempt for email: {}", request.getEmail());
            throw new IllegalArgumentException("Invalid email or password.");
        }

        logger.info("Successful login for user id: {}, email: {}, role: {}", user.getId(), user.getEmail(), user.getRole());
        return user;
    }
}
