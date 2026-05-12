package com.example.smart_notification_system.controller;

import com.example.smart_notification_system.entity.User;
import com.example.smart_notification_system.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for user profile management.
 * Allows users to update their personal Discord Webhook URL.
 */
@RestController
@RequestMapping("/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    /** Updates a user's personal Discord Webhook URL. */
    @PutMapping("/{id}/webhook")
    public ResponseEntity<User> updateWebhook(@PathVariable Long id, @RequestBody String webhookUrl) {
        return userRepository.findById(id).map(user -> {
            String cleanUrl = webhookUrl.replace("\"", "");
            user.setDiscordWebhookUrl(cleanUrl);
            return ResponseEntity.ok(userRepository.save(user));
        }).orElse(ResponseEntity.notFound().build());
    }

    /** Retrieves a user's profile by ID. */
    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable Long id) {
        return userRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
