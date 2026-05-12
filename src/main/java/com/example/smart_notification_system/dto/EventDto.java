package com.example.smart_notification_system.dto;

import java.util.Map;

/**
 * Data Transfer Object for incoming system events.
 * Contains the event type, a reference ID, and a flexible key-value data map.
 */
public class EventDto {

    private String eventType;
    private String referenceId;
    private Map<String, Object> data;

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

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }
}
