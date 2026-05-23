# Presentation Deck for Smart Notification System

---

## Slide 1: Title

**SMART NOTIFICATION SYSTEM**
A Real‑Time Middleware for Event Processing & Multi‑Channel Routing

---

## Slide 2: Introduction
- Built during an internship at **EchoBrain Pvt. Ltd.** (May 2026)
- Addresses scattered notification logic in micro‑service environments
- Provides a centralized, scalable alerting engine with live dashboard

---

## Slide 3: Problem Statement
- **Scattered Logic** – each service hard‑codes its own alerts
- **Channel Rigidity** – changing destinations requires code changes
- **Visibility Gap** – no central view of alert frequency or health

---

## Slide 4: Objectives
- Decouple notification handling from business services
- Enable dynamic rule configuration via a web UI
- Deliver alerts to **Discord** (and future channels) in real time
- Offer an admin dashboard with live analytics

---

## Slide 5: Methodology (Architecture Diagram)
```mermaid
flowchart LR
    subgraph Frontend
        UI[Dashboard UI]
    end
    subgraph Backend
        API[REST API]
        RuleEngine[SpEL Rule Engine]
        DB[(MySQL DB)]
        WS[WebSocket (STOMP)]
        Discord[Discord Webhook]
    end
    UI -->|WebSocket| WS
    UI -->|HTTP| API
    API --> RuleEngine
    RuleEngine --> DB
    RuleEngine --> Discord
    RuleEngine --> WS
```
---

## Slide 6: Implementation Highlights
- **Backend**: Spring Boot 3.2, Java 17, SpEL for dynamic rule evaluation
- **Database**: MySQL 8.0 with Hibernate ORM
- **Real‑Time**: STOMP + SockJS broadcasting to the UI
- **Delivery**: Prioritized Discord webhook routing (Rule → User → Global)
- **Security**: BCrypt password hashing, RBAC via Spring Security

---

## Slide 7: Results
- Live dashboard shows alerts instantly without polling
- Rule engine processes events in < 50 ms per rule
- Successfully migrated from HTTP polling to WebSockets, cutting network traffic by ~70 %
- System deployed on Render (Docker, multi‑stage build) and live on the internet

---

## Slide 8: Advantages & Disadvantages
**Advantages**
- Centralized rule management
- Extensible delivery channels
- Real‑time visibility
- Secure, role‑based access

**Disadvantages**
- Currently limited to Discord (other channels planned)
- Single‑node deployment; horizontal scaling needs work
- Rule complexity can impact evaluation latency

---

## Slide 9: Future Scope
- **Kafka Integration** for high‑throughput event ingestion
- **Multi‑Platform Delivery**: Slack, Teams, SMS (Twilio)
- **Rate Limiting** per user to prevent spamming
- **Horizontal Scaling** with Kubernetes & auto‑scaling
- AI‑driven suggestion of rule templates

---

## Slide 10: Thank You
- Questions?
- Contact: **Sanskriti Singh** – EchoBrain Pvt. Ltd.
- Email: s.singh@echobrain.com

---
