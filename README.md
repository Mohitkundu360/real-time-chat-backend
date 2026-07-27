# 🚀 Real-Time Chat Backend using Spring Boot & Redis Pub/Sub

A backend-only real-time chat application built with **Spring Boot** and **Redis Pub/Sub**.

The application demonstrates how Redis Pub/Sub can be used for instant message broadcasting while Redis data structures are used for storing chat rooms, participants, and message history.

---

# Features

- ✅ Create Chat Rooms
- ✅ Join Existing Chat Rooms
- ✅ Send Messages
- ✅ Real-Time Message Broadcasting using Redis Pub/Sub
- ✅ Retrieve Chat History
- ✅ Redis In-Memory Storage
- ✅ REST APIs

---

# Tech Stack

- Java 17
- Spring Boot 3
- Spring Data Redis
- Redis Pub/Sub
- Maven
- Docker
- REST API

---

# Project Structure

```
src
├── config
│   └── RedisConfig.java
├── controller
│   └── ChatRoomController.java
├── model
├── pubsub
│   ├── RedisMessagePublisher.java
│   ├── RedisMessagePublisherImpl.java
│   └── RedisMessageSubscriber.java
├── service
│   └── ChatRoomService.java
└── ChatappApplication.java
```

---

# Architecture

```
Client
   │
REST APIs
   │
Spring Boot
   │
───────────────
Chat Service
───────────────
      │
      │
Redis
├── Pub/Sub
├── Chat Rooms
├── Participants
└── Message History
```

---

# Getting Started

## Clone Repository

```bash
git clone https://github.com/<your-username>/real-time-chat-backend.git

cd real-time-chat-backend
```

---

## Start Redis

### Docker

```bash
docker run --name redis-chat -p 6379:6379 -d redis
```

Verify

```bash
docker ps
```

---

## Configure Redis

application.properties

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

---

## Run Project

Windows

```powershell
.\mvnw.cmd spring-boot:run
```

Linux/Mac

```bash
./mvnw spring-boot:run
```

Application runs on

```
http://localhost:8080
```

---

# API Endpoints

## 1. Create Chat Room

POST

```
/api/chatapp/chatrooms
```

Body

```json
{
  "roomName":"engineering"
}
```

---

## 2. Join Chat Room

POST

```
/api/chatapp/chatrooms/engineering/join
```

Body

```json
{
  "participant":"Mohit"
}
```

---

## 3. Send Message

POST

```
/api/chatapp/chatrooms/engineering/messages
```

Body

```json
{
  "participant":"Mohit",
  "message":"Hello from portfolio demo"
}
```

---

## 4. Get Chat History

GET

```
/api/chatapp/chatrooms/engineering/messages?limit=10
```

---

# Testing

The APIs were tested using PowerShell.

Example

```powershell
Invoke-RestMethod `
-Uri "http://localhost:8080/api/chatapp/chatrooms" `
-Method POST `
-Headers @{"Content-Type"="application/json"} `
-Body '{"roomName":"engineering"}'
```

---

# Redis Pub/Sub Verification

Redis Monitor confirms message publishing:

```
PUBLISH chatroom
```

Spring Boot Subscriber receives:

```
Received message:
Mohit: Hello from portfolio demo
```

This verifies that Redis Pub/Sub is functioning correctly.

---

# Future Improvements

- WebSocket support for browser clients
- JWT Authentication
- User Registration/Login
- Private Messaging
- Persistent Database (PostgreSQL/MySQL)
- Docker Compose
- Unit & Integration Tests
- Swagger/OpenAPI Documentation

---

# Author

**Mohit Kundu**

Backend Software Engineer

GitHub:
https://github.com/Mohitkundu360