# 🚀 Real-Time Chat Backend using Spring Boot & Redis Pub/Sub

A backend-only real-time chat application built with **Spring Boot** and **Redis Pub/Sub** that demonstrates event-driven communication using Redis.

The application allows users to create chat rooms, join existing rooms, exchange messages in real time, and retrieve chat history using Redis as both the message broker and in-memory data store.

---

## ✨ Features

## Create Chat Room

![Create Room](docs/screenshots/create-room.png)

## Send Message

![Send Message](docs/screenshots/send-message.png)

## Redis Monitor

![Redis Monitor](docs/screenshots/redis-monitor.png)

## Subscriber Output

![Spring Console](docs/screenshots/spring-console.png)

---

# 🏗 Architecture

![Architecture](docs/architecture.png)

### Request Flow

```
Client
   │
REST API
   │
ChatRoomController
   │
ChatRoomService
   │
 ├───────────────┐
 │               │
 ▼               ▼
Redis Storage   Redis Publisher
                    │
                    ▼
              Redis Pub/Sub
                    │
                    ▼
          Redis Subscriber
                    │
                    ▼
             Console Output
```

---

# 📦 Redis Data Model

| Redis Key | Type | Purpose |
|------------|------|---------|
| `CHAT_ROOMS` | Hash | Stores available chat rooms |
| `CHAT_ROOM:{room}:PARTICIPANTS` | Set | Stores participants in a room |
| `CHAT_ROOM:{room}:MESSAGES` | List | Stores chat history |
| `chatroom` | Pub/Sub Channel | Broadcasts new chat messages |

---

# 📂 Project Structure

```
src/main/java/com/example/chatapp
│
├── config
│   └── RedisConfig.java
│
├── controller
│   └── ChatRoomController.java
│
├── model
│
├── pubsub
│   ├── RedisMessagePublisher.java
│   ├── RedisMessagePublisherImpl.java
│   └── RedisMessageSubscriber.java
│
├── service
│   └── ChatRoomService.java
│
└── ChatappApplication.java
```

---

# 🛠 Tech Stack

- Java 17
- Spring Boot 3
- Spring Data Redis
- Redis Pub/Sub
- Maven
- Docker
- REST APIs

---

# 🚀 Getting Started

## Prerequisites

- Java 17+
- Docker Desktop
- Maven (or Maven Wrapper)

---

## Clone Repository

```bash
git clone https://github.com/Mohitkundu360/real-time-chat-backend.git

cd real-time-chat-backend
```

---

## Start Redis

```bash
docker run --name redis-chat -p 6379:6379 -d redis
```

Verify:

```bash
docker ps
```

---

## Configure Redis

Update `application.properties`

```properties
spring.data.redis.host=localhost
spring.data.redis.port=6379
```

---

## Run the Application

Windows

```powershell
.\mvnw.cmd spring-boot:run
```

Linux / macOS

```bash
./mvnw spring-boot:run
```

Application starts on

```
http://localhost:8080
```

---

# 📖 REST APIs

## 1️⃣ Create Chat Room

**POST**

```
/api/chatapp/chatrooms
```

Request

```json
{
  "roomName":"engineering"
}
```

Response

```json
{
  "message":"Chat room 'engineering' created successfully.",
  "roomId":"engineering",
  "status":"success"
}
```

---

## 2️⃣ Join Chat Room

**POST**

```
/api/chatapp/chatrooms/engineering/join
```

Request

```json
{
  "participant":"Mohit"
}
```

Response

```json
{
  "message":"User 'Mohit' joined chat room 'engineering'.",
  "status":"success"
}
```

---

## 3️⃣ Send Message

**POST**

```
/api/chatapp/chatrooms/engineering/messages
```

Request

```json
{
  "participant":"Mohit",
  "message":"Hello from portfolio demo"
}
```

Response

```json
{
  "message":"Message sent successfully.",
  "status":"success"
}
```

---

## 4️⃣ Retrieve Chat History

**GET**

```
/api/chatapp/chatrooms/engineering/messages?limit=10
```

---

# 🧪 Testing

The APIs were verified locally using **PowerShell**.

Example:

```powershell
Invoke-RestMethod `
-Uri "http://localhost:8080/api/chatapp/chatrooms" `
-Method POST `
-Headers @{"Content-Type"="application/json"} `
-Body '{"roomName":"engineering"}'
```

---

# 📡 Redis Pub/Sub Verification

Redis MONITOR confirms message publishing.

```
SUBSCRIBE chatroom

PUBLISH chatroom
```

Spring Boot Subscriber receives

```
Received message:

Mohit: Hello from portfolio demo
```

Redis also stores chat history using Redis Lists.

---

# 📸 Project Screenshots

## Create Chat Room

> docs/screenshots/create-room.png

## Join Chat Room

> docs/screenshots/join-room.png

## Send Message

> docs/screenshots/send-message.png

## Redis Pub/Sub Monitor

> docs/screenshots/redis-monitor.png

## Spring Boot Subscriber

> docs/screenshots/spring-console.png

---

# 📌 Current Limitations

- No authentication
- No WebSocket client
- Uses Redis as the primary storage
- No persistent relational database
- No automated test suite

---

# 🚀 Future Improvements

- JWT Authentication
- WebSocket/STOMP support
- PostgreSQL persistence
- Docker Compose setup
- Unit & Integration Tests
- Swagger / OpenAPI Documentation
- Multi-room WebSocket client

---

# 👨‍💻 Author

**Mohit Kundu**

Backend Software Engineer

GitHub: https://github.com/Mohitkundu360