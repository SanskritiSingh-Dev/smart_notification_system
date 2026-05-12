package com.example.smart_notification_system.controller;

import com.example.smart_notification_system.dto.EventDto;
import com.example.smart_notification_system.service.EventProcessingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for receiving incoming system events.
 * Events are evaluated against active rules to generate notifications.
 */
@RestController
@RequestMapping("/events")
public class EventController {

    @Autowired
    private EventProcessingService eventProcessingService;

    /** Receives and processes an incoming event. */
    @PostMapping
    public ResponseEntity<String> createEvent(@RequestBody EventDto eventDto) {
        eventProcessingService.processEvent(eventDto);
        return ResponseEntity.ok("Event processed successfully");
    }
}
