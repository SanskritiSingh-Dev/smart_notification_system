package com.example.smart_notification_system.controller;

import com.example.smart_notification_system.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST controller exposing notification analytics and statistics.
 */
@RestController
@RequestMapping("/analytics")
public class AnalyticsController {

    @Autowired
    private NotificationRepository notificationRepository;

    /** Returns notification counts grouped by event type for chart visualization. */
    @GetMapping("/stats")
    public ResponseEntity<List<Map<String, Object>>> getStats() {
        return ResponseEntity.ok(notificationRepository.countByType());
    }
}
