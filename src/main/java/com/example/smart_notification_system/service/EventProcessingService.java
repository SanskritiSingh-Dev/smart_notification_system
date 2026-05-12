package com.example.smart_notification_system.service;

import com.example.smart_notification_system.dto.EventDto;
import com.example.smart_notification_system.entity.Event;
import com.example.smart_notification_system.entity.Notification;
import com.example.smart_notification_system.entity.Rule;
import com.example.smart_notification_system.entity.User;
import com.example.smart_notification_system.repository.EventRepository;
import com.example.smart_notification_system.repository.UserRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * Core engine that processes incoming events against active rules.
 * When a rule's SpEL condition evaluates to true, a notification is generated
 * and routed to the appropriate Discord channel.
 */
@Service
public class EventProcessingService {

    private static final Logger log = LoggerFactory.getLogger(EventProcessingService.class);

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private RuleService ruleService;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    /**
     * Processes an incoming event by persisting it, then evaluating all active rules.
     *
     * @param eventDto the incoming event data transfer object
     */
    public void processEvent(EventDto eventDto) {
        // Persist the raw event
        Event event = new Event();
        event.setEventType(eventDto.getEventType());
        event.setReferenceId(eventDto.getReferenceId());
        try {
            event.setData(objectMapper.writeValueAsString(eventDto.getData()));
        } catch (JsonProcessingException e) {
            log.error("Failed to serialize event data: {}", e.getMessage());
        }
        eventRepository.save(event);

        // Evaluate all active rules against this event
        List<Rule> activeRules = ruleService.getActiveRules();
        for (Rule rule : activeRules) {
            if (rule.getConditionType().equals(eventDto.getEventType())) {
                if (evaluateCondition(rule, eventDto)) {
                    generateNotification(rule, eventDto);
                }
            }
        }
    }

    /**
     * Evaluates a rule's SpEL condition against the event data.
     *
     * @param rule     the rule containing the SpEL expression
     * @param eventDto the event providing variable values
     * @return true if the condition matches
     */
    private boolean evaluateCondition(Rule rule, EventDto eventDto) {
        try {
            ExpressionParser parser = new SpelExpressionParser();
            Expression exp = parser.parseExpression(rule.getConditionValue());
            StandardEvaluationContext context = new StandardEvaluationContext();

            if (eventDto.getData() != null) {
                for (Map.Entry<String, Object> entry : eventDto.getData().entrySet()) {
                    context.setVariable(entry.getKey(), entry.getValue());
                }
            }

            Boolean result = exp.getValue(context, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Failed to evaluate rule '{}': {}", rule.getName(), e.getMessage());
            return false;
        }
    }

    /**
     * Generates and dispatches a notification when a rule matches.
     * Uses the rule's message template with {{placeholder}} substitution.
     *
     * @param rule     the matched rule
     * @param eventDto the event that triggered the match
     */
    private void generateNotification(Rule rule, EventDto eventDto) {
        if (eventDto.getReferenceId() == null) return;

        try {
            Long userId = Long.parseLong(eventDto.getReferenceId());
            User user = userRepository.findById(userId).orElse(null);

            if (user != null) {
                Notification notification = new Notification();
                notification.setUser(user);

                // Build message from template or fallback
                String message = rule.getMessageTemplate();
                if (message == null || message.trim().isEmpty()) {
                    message = "Alert! Rule '" + rule.getName() + "' was triggered by event: " + eventDto.getEventType();
                } else {
                    // Replace {{key}} placeholders with actual data values
                    if (eventDto.getData() != null) {
                        for (Map.Entry<String, Object> entry : eventDto.getData().entrySet()) {
                            message = message.replace("{{" + entry.getKey() + "}}", entry.getValue().toString());
                        }
                    }
                }

                notification.setMessage(message);
                notification.setType(eventDto.getEventType());
                notification.setCreatedTime(LocalDateTime.now());
                notificationService.createNotification(notification, rule.getWebhookUrl());
            }
        } catch (NumberFormatException e) {
            log.error("Invalid reference ID for user lookup: {}", eventDto.getReferenceId());
        }
    }
}
