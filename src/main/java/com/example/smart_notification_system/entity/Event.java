package com.example.smart_notification_system.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Event entity representing an incoming system event.
 * Events are evaluated against active Rules to determine
 * if a Notification should be generated.
 */
@Entity
@Table(name = "events")
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /** Event category (e.g., PURCHASE, SECURITY, FINANCE). */
    @Column(name = "event_type")
    private String eventType;

    /** Reference identifier linking to a user or resource. */
    @Column(name = "reference_id")
    private String referenceId;

    /** Raw event payload stored as a JSON string. */
    @Column(columnDefinition = "TEXT")
    private String data;

    @Column(name = "created_time")
    private LocalDateTime createdTime;

    @PrePersist
    public void prePersist() {
        this.createdTime = LocalDateTime.now();
    }

    // ── Getters & Setters ──────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public String getReferenceId() {
        return referenceId;
    }

    public void setReferenceId(String referenceId) {
        this.referenceId = referenceId;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public LocalDateTime getCreatedTime() {
        return createdTime;
    }

    public void setCreatedTime(LocalDateTime createdTime) {
        this.createdTime = createdTime;
    }
}
