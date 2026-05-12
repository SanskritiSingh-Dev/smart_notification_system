package com.example.smart_notification_system.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;

/**
 * Service responsible for delivering notifications to Discord via Webhook API.
 * Supports personalized routing with a 3-tier priority system:
 *   1. Rule-level webhook (highest priority)
 *   2. User-level webhook
 *   3. Global default webhook (fallback)
 */
@Service
public class DiscordService {

    private static final Logger log = LoggerFactory.getLogger(DiscordService.class);

    @Value("${discord.webhook.url}")
    private String defaultWebhookUrl;

    private final RestTemplate restTemplate;

    public DiscordService() {
        this.restTemplate = new RestTemplate();
    }

    /**
     * Sends a rich-embed notification to Discord.
     *
     * @param message          the notification message body
     * @param targetWebhookUrl optional override webhook URL (rule-level or user-level)
     */
    @Retryable(value = Exception.class, maxAttempts = 3, backoff = @Backoff(delay = 2000))
    @Async
    public void sendNotification(String message, String targetWebhookUrl) {
        String finalUrl = (targetWebhookUrl != null && !targetWebhookUrl.isEmpty())
                ? targetWebhookUrl
                : defaultWebhookUrl;

        if (finalUrl == null || finalUrl.isEmpty()) {
            log.warn("No Discord Webhook URL configured. Skipping notification delivery.");
            return;
        }

        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            // Auto-detect severity based on message keywords
            int color = 5814783; // Default: Blue
            String emoji = "🔔";

            String lowerMessage = message.toLowerCase();
            if (lowerMessage.contains("emergency") || lowerMessage.contains("critical") || lowerMessage.contains("problem")) {
                color = 15158332; // Red
                emoji = "🚨";
            } else if (lowerMessage.contains("success") || lowerMessage.contains("bought") || lowerMessage.contains("working")) {
                color = 3066993; // Green
                emoji = "✅";
            }

            // Build Discord rich-embed payload
            Map<String, Object> payload = new HashMap<>();
            payload.put("content", emoji + " **" + message + "**");

            Map<String, Object> embed = new HashMap<>();
            embed.put("title", "New Alert Triggered");
            embed.put("description", message);
            embed.put("color", color);

            Map<String, String> footer = new HashMap<>();
            footer.put("text", "Production Middleware System • Smart Alerts");
            embed.put("footer", footer);

            payload.put("embeds", new Object[]{embed});

            HttpEntity<Map<String, Object>> request = new HttpEntity<>(payload, headers);

            log.info("Sending Discord notification to: {}...", finalUrl.substring(0, Math.min(finalUrl.length(), 40)));
            restTemplate.postForEntity(finalUrl, request, String.class);
            log.info("Discord notification delivered successfully.");
        } catch (Exception e) {
            log.error("Failed to deliver Discord notification: {}", e.getMessage());
        }
    }
}
