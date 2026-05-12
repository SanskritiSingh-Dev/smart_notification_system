package com.example.smart_notification_system.service;

import com.example.smart_notification_system.dto.RegisterRequest;
import com.example.smart_notification_system.entity.User;
import com.example.smart_notification_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Service layer for user registration and lookup.
 */
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    /**
     * Registers a new user with an encoded password.
     * Defaults to ROLE_USER if no role is specified.
     */
    public User register(RegisterRequest request) {
        String role = (request.getRole() == null || request.getRole().isEmpty())
                ? "ROLE_USER"
                : request.getRole();

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(role);
        return userRepository.save(user);
    }

    /** Finds a user by their email address. */
    public Optional<User> findUserByEmail(String email) {
        return userRepository.findByEmail(email);
    }
}
