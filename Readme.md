# Scalable Notification Service

A production-grade async notification service built with **Java**, **Spring Boot**, and **Kafka**. Supports multi-channel delivery (Email, SMS), retry logic, Dead Letter Queue, Redis rate limiting, and full delivery status tracking.

---

## Architecture

```
REST API → Kafka Producer → Kafka Topics → Consumers → Email / SMS
                                                ↓
                                         PostgreSQL (status tracking)
                                                ↓
                               Retry (exponential backoff) → DLT
```

---

## Features

- **Multi-channel notifications** — Email (Gmail SMTP) and SMS (pluggable with Twilio/AWS SNS)
- **Async processing** — Kafka producer-consumer decouples request from delivery
- **Delivery status tracking** — PostgreSQL tracks PENDING → SENT → FAILED
- **Retry with exponential backoff** — 3 retries with 2s, 4s, 8s delays
- **Dead Letter Queue (DLT)** — Permanently failed messages routed to `notification.email.DLT`
- **Redis rate limiting** — Max 5 notifications per recipient per hour
- **Unit tests** — JUnit + Mockito covering happy path, rate limit, and failure scenarios
- **Dockerized** — Full stack runs with a single `docker-compose up`

---

## Tech Stack

| Layer | Technology |
|---|---|
| Backend | Java 21, Spring Boot |
| Messaging | Apache Kafka |
| Database | PostgreSQL |
| Cache / Rate Limiting | Redis |
| Email | JavaMailSender (Gmail SMTP) |
| SMS | Mock (pluggable with Twilio/AWS SNS) |
| Testing | JUnit 5, Mockito |
| DevOps | Docker, Docker Compose |

---

## Getting Started

### Prerequisites
- Java 21
- Docker Desktop
- Maven

### Run Locally

**1. Start infrastructure:**
```bash
docker-compose up -d
```

**2. Run the application:**
```bash
mvn spring-boot:run
```

**3. Send a notification:**
```bash
POST http://localhost:8080/api/v1/notifications/send
Content-Type: application/json

{
  "recipient": "user@example.com",
  "subject": "Test Notification",
  "message": "Hello from Notification Service!",
  "channel": "EMAIL"
}
```

---

## API

### Send Notification
**POST** `/api/v1/notifications/send`

| Field | Type | Description |
|---|---|---|
| recipient | String | Email address or phone number |
| subject | String | Notification subject |
| message | String | Notification content |
| channel | Enum | EMAIL, SMS, PUSH |

**Responses:**
- `200 OK` — Notification queued successfully
- `429 Too Many Requests` — Rate limit exceeded (max 5/hour per recipient)

---

## Project Structure

```
src/main/java/com/kiran/notificationservice/
├── controller/         # REST API layer
├── service/            # Business logic (Email, SMS, RateLimiter)
├── kafka/              # Producer and Consumer
├── entity/             # NotificationLog JPA entity
├── dto/                # NotificationRequest DTO
├── repository/         # Spring Data JPA repository
├── enums/              # NotificationChannel, NotificationStatus
└── config/             # Kafka config (retry, DLT)
```

---

## Configuration

Key properties in `application.properties`:

```properties
# Kafka
spring.kafka.bootstrap-servers=localhost:9092

# PostgreSQL
spring.datasource.url=jdbc:postgresql://localhost:5433/notificationdb

# Redis
spring.data.redis.host=localhost
spring.data.redis.port=6379

# Gmail SMTP
spring.mail.host=smtp.gmail.com
spring.mail.port=587
```

---

## How Rate Limiting Works

Redis tracks notification count per recipient with a 1-hour TTL:
```
Key: rate_limit:notifications:<recipient>
Value: count (incremented atomically)
Expiry: 1 hour (set on first request)
```

---

## How Retry + DLT Works

Failed messages are retried with exponential backoff:
```
Attempt 1 → wait 2 seconds
Attempt 2 → wait 4 seconds
Attempt 3 → wait 8 seconds
All failed → route to notification.email.DLT
```