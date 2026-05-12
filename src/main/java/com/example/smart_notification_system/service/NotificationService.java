package com.example.smart_notification_system.service;

import com.example.smart_notification_system.entity.Notification;
import com.example.smart_notification_system.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing Notification lifecycle.
 * Handles creation, reading, marking as read, and broadcasting via WebSockets.
 */
@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private DiscordService discordService;

    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    /**
     * Creates a notification, broadcasts it via WebSocket, and delivers it to Discord.
     *
     * @param notification    the notification to persist and deliver
     * @param overrideWebhook optional rule-level webhook that takes highest priority
     * @return the saved notification
     */
    public Notification createNotification(Notification notification, String overrideWebhook) {
        Notification saved = notificationRepository.save(notification);

        // Broadcast via WebSocket for real-time dashboard updates
        if (notification.getUser() != null) {
            String topic = "/topic/notifications/" + notification.getUser().getId();
            messagingTemplate.convertAndSend(topic, saved);
        }

        // Webhook priority: Rule-level > User-level > Global default
        String personalWebhook = (notification.getUser() != null)
                ? notification.getUser().getDiscordWebhookUrl()
                : null;
        String finalWebhook = (overrideWebhook != null && !overrideWebhook.isEmpty())
                ? overrideWebhook
                : personalWebhook;

        discordService.sendNotification(notification.getMessage(), finalWebhook);

        return saved;
    }

    /** Returns all notifications for a specific user. */
    public List<Notification> getNotificationsForUser(Long userId) {
        return notificationRepository.findByUserId(userId);
    }

    /** Returns all notifications across all users (Admin view). */
    public List<Notification> getAllNotifications() {
        return notificationRepository.findAll();
    }

    /** Marks a single notification as READ. */
    public Optional<Notification> markAsRead(Long id) {
        return notificationRepository.findById(id).map(notification -> {
            notification.setStatus("READ");
            return notificationRepository.save(notification);
        });
    }

    /** Marks all notifications as READ (bulk operation). */
    public void markAllAsRead() {
        List<Notification> all = notificationRepository.findAll();
        all.forEach(n -> n.setStatus("READ"));
        notificationRepository.saveAll(all);
    }
}
