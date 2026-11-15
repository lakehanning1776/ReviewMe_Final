package com.example.demo.service;

import com.example.demo.dto.AuthRequest;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Map<String, String> register(AuthRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank()
                || request.getPassword() == null || request.getPassword().isBlank()) {
            return Map.of("message", "Username and password are required");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            return Map.of("message", "Username already exists");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());

        User user = new User(request.getUsername(), hashedPassword);
        userRepository.save(user);

        return Map.of("message", "User registered successfully");
    }

    public java.util.Optional<User> authenticate(String username, String password) {
        if (username == null || password == null) return java.util.Optional.empty();
        return userRepository.findByUsername(username.trim())
                .filter(user -> passwordEncoder.matches(password, user.getPassword()));
    }

    public Map<String, String> login(AuthRequest request) {
        return authenticate(request.getUsername(), request.getPassword())
                .map(user -> Map.of("message", "Login successful"))
                .orElse(Map.of("message", "Invalid username or password"));
    }
}