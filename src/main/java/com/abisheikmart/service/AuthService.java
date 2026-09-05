package com.abisheikmart.service;

import com.abisheikmart.dto.LoginRequest;
import com.abisheikmart.dto.RegisterRequest;
import com.abisheikmart.model.User;
import com.abisheikmart.repository.UserRepository;
import com.abisheikmart.util.CryptoUtils;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final CryptoUtils cryptoUtils;

    public AuthService(UserRepository userRepository, CryptoUtils cryptoUtils) {
        this.userRepository = userRepository;
        this.cryptoUtils = cryptoUtils;
    }

    public Map<String, Object> register(RegisterRequest req) {
        if (req.getEmail() == null || req.getEmail().isBlank() || req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("Email and password are required");
        }
        if (userRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("User with this email already exists");
        }

        String role = (req.getRole() != null && req.getRole().equalsIgnoreCase("SELLER")) ? "SELLER" : "BUYER";
        String salt = cryptoUtils.generateSalt();
        String passwordHash = cryptoUtils.hashPassword(req.getPassword(), salt);

        User user = new User();
        user.setName(req.getName() != null && !req.getName().isBlank() ? req.getName() : req.getEmail().split("@")[0]);
        user.setEmail(req.getEmail());
        user.setPasswordHash(passwordHash);
        user.setSalt(salt);
        user.setRole(role);

        User savedUser = userRepository.save(user);
        String token = cryptoUtils.createSession(savedUser);

        return Map.of("token", token, "user", savedUser);
    }

    public Map<String, Object> login(LoginRequest req) {
        String email = req.getEmail();
        if (email == null || email.isBlank() || req.getPassword() == null || req.getPassword().isBlank()) {
            throw new IllegalArgumentException("Email and password are required");
        }

        Optional<User> userOpt = userRepository.findByEmail(email);
        if (userOpt.isEmpty()) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        User user = userOpt.get();
        String hash = cryptoUtils.hashPassword(req.getPassword(), user.getSalt());

        if (!hash.equalsIgnoreCase(user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        String token = cryptoUtils.createSession(user);
        return Map.of("token", token, "user", user);
    }
}

