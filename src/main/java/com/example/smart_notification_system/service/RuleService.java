package com.example.smart_notification_system.service;

import com.example.smart_notification_system.entity.Rule;
import com.example.smart_notification_system.repository.RuleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Service layer for managing Rule CRUD operations.
 */
@Service
public class RuleService {

    @Autowired
    private RuleRepository ruleRepository;

    public Rule createRule(Rule rule) {
        return ruleRepository.save(rule);
    }

    public List<Rule> getAllRules() {
        return ruleRepository.findAll();
    }

    public List<Rule> getActiveRules() {
        return ruleRepository.findByIsActiveTrue();
    }

    public Optional<Rule> getRuleById(Long id) {
        return ruleRepository.findById(id);
    }

    /** Partially updates a rule, preserving any fields not provided. */
    public Rule updateRule(Long id, Rule updatedRule) {
        return ruleRepository.findById(id).map(rule -> {
            if (updatedRule.getName() != null) rule.setName(updatedRule.getName());
            if (updatedRule.getConditionType() != null) rule.setConditionType(updatedRule.getConditionType());
            if (updatedRule.getConditionValue() != null) rule.setConditionValue(updatedRule.getConditionValue());
            if (updatedRule.getMessageTemplate() != null) rule.setMessageTemplate(updatedRule.getMessageTemplate());
            if (updatedRule.getWebhookUrl() != null) rule.setWebhookUrl(updatedRule.getWebhookUrl());
            if (updatedRule.getTargetChannel() != null) rule.setTargetChannel(updatedRule.getTargetChannel());
            rule.setActive(updatedRule.isActive());
            return ruleRepository.save(rule);
        }).orElseThrow(() -> new RuntimeException("Rule not found with ID: " + id));
    }

    public void deleteRule(Long id) {
        ruleRepository.deleteById(id);
    }
}
