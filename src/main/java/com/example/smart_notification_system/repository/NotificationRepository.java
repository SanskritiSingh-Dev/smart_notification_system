package com.example.smart_notification_system.repository;

import com.example.smart_notification_system.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;

/**
 * Repository for Notification entity with analytics query support.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByUserId(Long userId);

    List<Notification> findByUserIdAndStatus(Long userId, String status);

    /** Aggregates notification counts grouped by event type for analytics charts. */
    @Query("SELECT n.type as type, COUNT(n) as count FROM Notification n GROUP BY n.type")
    List<Map<String, Object>> countByType();
}
