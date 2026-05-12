<p align="center">
  <img src="https://img.shields.io/badge/Spring%20Boot-3.2.5-6DB33F?style=for-the-badge&logo=spring-boot" />
  <img src="https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk" />
  <img src="https://img.shields.io/badge/MySQL-8.0-4479A1?style=for-the-badge&logo=mysql&logoColor=white" />
  <img src="https://img.shields.io/badge/WebSockets-STOMP-010101?style=for-the-badge&logo=socket.io" />
  <img src="https://img.shields.io/badge/Discord-Webhooks-5865F2?style=for-the-badge&logo=discord&logoColor=white" />
</p>

<h1 align="center">🔔 Smart Notification System</h1>

<p align="center">
  <strong>A real-time, multi-channel notification middleware built with Spring Boot.</strong><br/>
  Route intelligent alerts to the right people, on the right channel, at the right time.
</p>

<p align="center">
  <a href="#-features">Features</a> •
  <a href="#-system-architecture">Architecture</a> •
  <a href="#-tech-stack--why">Tech Stack</a> •
  <a href="#-getting-started">Getting Started</a> •
  <a href="#-api-reference">API</a> •
  <a href="#-challenges--solutions">Challenges</a> •
  <a href="#-future-scope">Future Scope</a>
</p>

---

## 📖 Introduction

**Smart Notification System** is a production-grade notification middleware that processes real-time events, evaluates them against configurable rules, and delivers personalized alerts across multiple Discord channels — all through a sleek, cinematic admin dashboard.

Think of it as the **"Brain"** sitting between your applications and your communication channels. Instead of hardcoding alert logic into every app, you define rules once, and the system handles the rest — routing, formatting, and delivering notifications intelligently.

> **Built as a Major Project during my internship at EchoBrain Pvt. Ltd.**

---

## 🤔 Why I Built This

In modern organizations, critical events (security breaches, purchase confirmations, system failures) happen across dozens of systems. The problem? **There's no centralized way to manage, route, and deliver these alerts.**

### The Problem:
- Developers hardcode notification logic into every application
- There's no way to route different alert types to different channels
- Users have no control over where they receive their alerts
- There's no visibility into which alerts are firing the most

### My Solution:
A **standalone middleware** that any application can call via a simple REST API. The middleware evaluates rules, routes notifications to the correct Discord channel, and provides real-time analytics — all without touching the source application's code.

---

## ✨ Features

### 🧠 SpEL-Based Rule Engine
- Define dynamic rules using **Spring Expression Language (SpEL)**
- Rules evaluate complex conditions like `#amount > 1000` or `#severity == 'CRITICAL'`
- Supports `{{placeholder}}` message templates for dynamic content

### 📡 Multi-Channel Routing (3-Tier Priority)
```
Rule Webhook (highest) → User Webhook → Global Default (fallback)
```
- Each **Rule** can have its own dedicated Discord channel
- Each **User** can set their personal webhook for private alerts
- System falls back to a global channel if nothing else is configured

### ⚡ Real-Time Dashboard (WebSockets)
- Notifications appear **instantly** on the dashboard via STOMP/SockJS
- No polling or page refresh needed — it's truly live
- Each user subscribes to their own private topic

### 📊 Live Analytics
- Dynamic **Chart.js** bar chart showing notification distribution by type
- Chart updates in real-time as new alerts are triggered
- Helps admins identify which event types are most active

### 🎨 Premium UI
- Glassmorphism-styled dashboard with smooth animations
- Custom confirmation modals (no browser popups)
- Responsive layout that works on desktop, tablet, and mobile
- Markdown rendering for bold text in notifications

### 🔒 Security
- **BCrypt** password hashing
- **Role-based access control** (ADMIN vs USER)
- `@JsonIgnore` on password fields — never exposed in API responses
- Custom 401 handler — no browser login popup interruptions
- Environment variable support for secrets

---

## 🏗 System Architecture

```
┌─────────────────────────────────────────────────────────┐
│                    CLIENT (Browser)                      │
│  ┌─────────────┐  ┌──────────────┐  ┌───────────────┐  │
│  │  Auth Forms  │  │  Dashboard   │  │  Analytics    │  │
│  │  (Login/Reg) │  │  (Live Feed) │  │  (Chart.js)   │  │
│  └──────┬───────┘  └──────┬───────┘  └───────┬───────┘  │
│         │                 │ WebSocket         │          │
└─────────┼─────────────────┼───────────────────┼──────────┘
          │ REST            │ STOMP             │ REST
          ▼                 ▼                   ▼
┌─────────────────────────────────────────────────────────┐
│              SPRING BOOT APPLICATION                     │
│                                                          │
│  ┌──────────────────────────────────────────────────┐   │
│  │              CONTROLLER LAYER                     │   │
│  │  AuthController · RuleController · EventController│   │
│  │  NotificationController · UserController          │   │
│  │  AnalyticsController                              │   │
│  └──────────────────────┬────────────────────────────┘   │
│                         │                                │
│  ┌──────────────────────▼────────────────────────────┐   │
│  │               SERVICE LAYER                        │   │
│  │  UserService · RuleService · NotificationService   │   │
│  │  EventProcessingService · DiscordService           │   │
│  └──────────┬───────────────────────┬────────────────┘   │
│             │                       │                    │
│  ┌──────────▼──────────┐  ┌────────▼─────────────────┐  │
│  │   REPOSITORY LAYER  │  │   EXTERNAL INTEGRATIONS  │  │
│  │  JPA / Hibernate    │  │   Discord Webhook API    │  │
│  │  JPQL Analytics     │  │   WebSocket (STOMP)      │  │
│  └──────────┬──────────┘  └──────────────────────────┘  │
│             │                                            │
└─────────────┼────────────────────────────────────────────┘
              │
     ┌────────▼────────┐
     │   MySQL 8.0     │
     │  ┌───────────┐  │
     │  │  users    │  │
     │  │  rules    │  │
     │  │  events   │  │
     │  │notifications│ │
     │  └───────────┘  │
     └─────────────────┘
```

---

## 🛠 Tech Stack & Why

| Technology | Purpose | Why This Choice |
|---|---|---|
| **Java 17** | Core Language | Industry standard for enterprise backends; strong typing catches bugs at compile time |
| **Spring Boot 3.2** | Backend Framework | Auto-configuration, embedded server, massive ecosystem — the gold standard for Java REST APIs |
| **Spring Security** | Authentication | Battle-tested security framework with BCrypt, role-based access, and custom entry points |
| **Spring WebSocket** | Real-Time | STOMP over SockJS gives instant push updates without polling |
| **SpEL** | Rule Engine | Built into Spring; evaluates dynamic expressions without external libraries |
| **MySQL 8.0** | Database | Relational data (users, rules, notifications) with ACID compliance |
| **JPA / Hibernate** | ORM | Eliminates raw SQL; entity relationships map naturally to Java objects |
| **Discord Webhooks** | Delivery Channel | Zero-config message delivery; supports rich embeds with colors and formatting |
| **Chart.js** | Analytics | Lightweight, beautiful charts that integrate easily with vanilla JS |
| **Vanilla JS/CSS** | Frontend | No framework overhead; glassmorphism UI with smooth animations |

---

## 🚀 Getting Started

### Prerequisites
- **Java 17** or higher
- **MySQL 8.0** running locally
- **Maven** (included via wrapper)

### 1. Clone the Repository
```bash
git clone https://github.com/YOUR_USERNAME/smart_notification_system.git
cd smart_notification_system
```

### 2. Create the Database
```sql
CREATE DATABASE notification_db;
```

### 3. Configure Environment (Optional)
The app works out of the box with defaults. For custom settings, set these environment variables:
```bash
DB_URL=jdbc:mysql://localhost:3306/notification_db
DB_USERNAME=root
DB_PASSWORD=your_password
DISCORD_WEBHOOK_URL=your_webhook_url
```

### 4. Run the Application
```bash
./mvnw spring-boot:run
```

### 5. Open the Dashboard
Navigate to: **http://localhost:8081**

---

## 📡 API Reference

### Authentication
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/register` | Register a new user/admin | Public |
| `POST` | `/login` | Authenticate and get user profile | Public |

### Notifications
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/notifications` | Get all notifications (Admin) | ADMIN |
| `GET` | `/notifications/user/{id}` | Get user's notifications | Authenticated |
| `PUT` | `/notifications/{id}/read` | Mark notification as read | Authenticated |
| `PUT` | `/notifications/read-all` | Mark all as read | Authenticated |

### Rules
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/rules` | Create a new rule | ADMIN |
| `GET` | `/rules` | List all rules | ADMIN |
| `PUT` | `/rules/{id}` | Update a rule | ADMIN |
| `DELETE` | `/rules/{id}` | Delete a rule | ADMIN |

### Events
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `POST` | `/events` | Submit an event for processing | Public |

### Analytics
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/analytics/stats` | Get notification counts by type | Authenticated |

### Users
| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| `GET` | `/users/{id}` | Get user profile | Authenticated |
| `PUT` | `/users/{id}/webhook` | Update user's Discord webhook | Authenticated |

---

## 🧗 Challenges & Solutions

### 1. Duplicate ID Conflict (Admin vs User)
**Problem:** Initially, `Admin` and `User` were separate database tables. Both had auto-incrementing IDs starting from 1, causing authentication conflicts.

**Solution:** Unified both into a single `User` table with a `role` field (`ROLE_ADMIN` / `ROLE_USER`). This eliminated ID overlap and simplified the entire authentication flow.

### 2. Browser Login Popup
**Problem:** Spring Security's default `httpBasic()` configuration triggers an intrusive browser popup when a 401 occurs, breaking the SPA experience.

**Solution:** Implemented a custom `AuthenticationEntryPoint` that returns a clean JSON 401 response instead of the WWW-Authenticate header that triggers the popup.

### 3. Notifications Not Routing to Correct Channel
**Problem:** All notifications were going to the same "General" Discord channel regardless of the user or rule configuration.

**Solution:** Implemented a 3-tier webhook priority system:
- **Rule-level webhook** (highest priority) → specific channel per rule
- **User-level webhook** → personal channel per user
- **Global default** → fallback channel

### 4. Dashboard Flickering on Auto-Refresh
**Problem:** The 5-second polling interval caused the notification list to "flash" every time it refreshed, creating a poor user experience.

**Solution:** Replaced polling with **WebSockets (STOMP/SockJS)** for zero-latency updates, and added DOM diffing logic that only updates the UI when content actually changes.

### 5. Hardcoded Secrets in Source Code
**Problem:** Database passwords and webhook URLs were hardcoded in `application.properties`, making it unsafe to push to public GitHub repositories.

**Solution:** Externalized all secrets using Spring's `${ENV_VAR:default}` syntax, allowing safe GitHub publishing while maintaining local development convenience.

---

## 🔮 Future Scope

| Feature | Description |
|---|---|
| **Multi-Channel Delivery** | Add Slack, Email, and SMS as delivery options alongside Discord |
| **Quiet Hours / DND** | Users can set "Do Not Disturb" windows; non-critical alerts queue until morning |
| **Priority-Based Styling** | Visual hierarchy (Red/Amber/Green) based on alert severity |
| **Audit Logs** | Track every admin action (rule created, user updated) for security compliance |
| **Scheduled Rules** | Rules that only activate during specific time windows |
| **Rate Limiting per User** | Prevent notification floods by throttling per-user delivery |
| **Docker Containerization** | Single `docker-compose up` for the entire stack |
| **Kubernetes Deployment** | Horizontal scaling for enterprise-grade throughput |

---

## 📁 Project Structure

```
smart_notification_system/
├── src/main/java/com/example/smart_notification_system/
│   ├── config/              # WebSocket configuration
│   ├── controller/          # REST API endpoints (6 controllers)
│   ├── dto/                 # Request/Response data shapes
│   ├── entity/              # JPA database models (4 entities)
│   ├── repository/          # Database access layer
│   ├── security/            # Auth config, BCrypt, role-based access
│   └── service/             # Business logic (5 services)
├── src/main/resources/
│   ├── static/              # Frontend (HTML, CSS, JS)
│   └── application.properties
└── pom.xml                  # Maven dependencies
```

---

## 👨‍💻 Author

**Sanskriti Singh**
- Internship Project at **EchoBrain Pvt. Ltd.**
- Built with Java, Spring Boot, and a lot of ☕

---

<p align="center">
  <strong>⭐ If you found this project useful, please give it a star!</strong>
</p>
