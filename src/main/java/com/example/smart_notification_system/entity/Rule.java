package com.example.smart_notification_system.entity;

import jakarta.persistence.*;

/**
 * Rule entity defining a notification trigger condition.
 * Each rule specifies an event type to match, a SpEL condition to evaluate,
 * a message template, and an optional webhook for channel-level routing.
 */
@Entity
@Table(name = "rules")
public class Rule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    /** Event type to match against (e.g., PURCHASE, SECURITY). */
    @Column(name = "condition_type")
    private String conditionType;

    /** SpEL expression to evaluate (e.g., #amount > 1000). */
    @Column(name = "condition_value")
    private String conditionValue;

    @Column(name = "is_active")
    private boolean isActive = true;

    /** Template message with {{placeholder}} syntax for dynamic values. */
    @Column(name = "message_template")
    private String messageTemplate;

    /** Optional rule-specific Discord Webhook URL for channel-level routing. */
    @Column(name = "webhook_url", length = 500)
    private String webhookUrl;

    /** Human-friendly label for the target channel (e.g., "Shopping Server"). */
    @Column(name = "target_channel")
    private String targetChannel;

    // ── Getters & Setters ──────────────────────────────────────────

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getConditionType() {
        return conditionType;
    }

    public void setConditionType(String conditionType) {
        this.conditionType = conditionType;
    }

    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getMessageTemplate() {
        return messageTemplate;
    }

    public void setMessageTemplate(String messageTemplate) {
        this.messageTemplate = messageTemplate;
    }

    public String getWebhookUrl() {
        return webhookUrl;
    }

    public void setWebhookUrl(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    public String getTargetChannel() {
        return targetChannel;
    }

    public void setTargetChannel(String targetChannel) {
        this.targetChannel = targetChannel;
    }
}
