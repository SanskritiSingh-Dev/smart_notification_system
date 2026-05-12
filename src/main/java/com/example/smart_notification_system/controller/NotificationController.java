package com.example.smart_notification_system.controller;

import com.example.smart_notification_system.entity.Notification;
import com.example.smart_notification_system.service.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST controller for notification retrieval and status management.
 */
@RestController
@RequestMapping("/notifications")
public class NotificationController {

    @Autowired
    private NotificationService notificationService;

    /** Returns all notifications across all users (Admin only). */
    @GetMapping
    public ResponseEntity<List<Notification>> getAllNotifications() {
        return ResponseEntity.ok(notificationService.getAllNotifications());
    }

    /** Returns notifications for a specific user. */
    @GetMapping("/user/{id}")
    public ResponseEntity<List<Notification>> getUserNotifications(@PathVariable Long id) {
        return ResponseEntity.ok(notificationService.getNotificationsForUser(id));
    }

    /** Marks a single notification as READ. */
    @PutMapping("/{id}/read")
    public ResponseEntity<Notification> markAsRead(@PathVariable Long id) {
        return notificationService.markAsRead(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    /** Marks all notifications as READ (bulk clear). */
    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllAsRead() {
        notificationService.markAllAsRead();
        return ResponseEntity.noContent().build();
    }
}
