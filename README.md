# Real-Time Chat Backend — Spring Boot, Redis & PostgreSQL

A backend-only, multi-room chat application built with **Java 17, Spring Boot, Redis Pub/Sub, and PostgreSQL**. The project demonstrates REST API design, event-driven messaging, relational persistence, containerized development, automated testing, API documentation, and load testing.

The application supports creating chat rooms, joining rooms, publishing messages, and retrieving chat history through REST endpoints.

## Features

* **Chat room management:** Create rooms and register participants.
* **Real-time message distribution:** Publish chat messages through Redis Pub/Sub.
* **PostgreSQL persistence:** Store chat history in a relational database.
* **Redis data management:** Maintain room-related state and support message distribution.
* **REST APIs:** Create rooms, join rooms, send messages, and retrieve history.
* **OpenAPI documentation:** Explore endpoints through Swagger UI.
* **Automated testing:** Maven tests with PostgreSQL and Redis integration support.
* **Containerization:** Docker and Docker Compose support.
* **Load testing:** k6 scripts and recorded results in `load-test/`.

## Architecture

```text
                    Client
                      |
                      v
                REST Controllers
                      |
                      v
                 Chat Services
                  /         \
                 v           v
          Redis Storage   PostgreSQL
          Room state      Chat history
                 |
                 v
           Redis Publisher
                 |
                 v
             Redis Pub/Sub
                 |
                 v
           Redis Subscriber
```

Redis and PostgreSQL serve different purposes: Redis supports room-related state and message distribution, while PostgreSQL provides relational persistence for chat history.

### Message flow

1. A client submits a request to a REST endpoint.
2. The controller delegates the operation to the service layer.
3. The application updates the relevant room state or persists chat history.
4. Messages are published through Redis Pub/Sub.
5. Redis subscribers receive published messages.

**Note:** Redis Pub/Sub provides message distribution, not durable message delivery guarantees. A disconnected subscriber can miss messages.

## Technology Stack

| Component           | Technology                              |
| ------------------- | --------------------------------------- |
| Language            | Java 17                                 |
| Framework           | Spring Boot 3                           |
| API layer           | Spring Web / REST                       |
| Messaging           | Redis Pub/Sub                           |
| In-memory state     | Redis                                   |
| Relational database | PostgreSQL                              |
| Persistence         | Spring Data JPA                         |
| Build               | Maven                                   |
| API documentation   | springdoc OpenAPI / Swagger UI          |
| Containers          | Docker, Docker Compose                  |
| Testing             | Spring Boot Test, JUnit, Testcontainers |
| Load testing        | k6                                      |
| CI/CD               | GitHub Actions                          |

## Project Structure

```text
real-time-chat-backend/
├── .github/
│   └── workflows/
├── docs/
│   ├── architecture.png
│   └── screenshots/
│       └── swagger-ui.png
├── load-test/
├── src/
│   ├── main/
│   │   └── java/com/example/chatapp/
│   │       ├── config/
│   │       ├── controller/
│   │       ├── model/
│   │       ├── pubsub/
│   │       ├── service/
│   │       └── ChatappApplication.java
│   └── test/
├── .env.example
├── Dockerfile
├── docker-compose.yml
├── mvnw
├── mvnw.cmd
├── pom.xml
└── README.md
```

The directory listing above describes the principal project components. Refer to the repository for the complete source and test file list.

## Getting Started

### Prerequisites

* Java 17 or a compatible configured JDK
* Docker Desktop with Docker Compose
* Git

The Maven Wrapper (`mvnw` / `mvnw.cmd`) is included, so a separate Maven installation is not normally required.

### 1. Clone the repository

```bash
git clone https://github.com/Mohitkundu360/real-time-chat-backend.git
cd real-time-chat-backend
```

To work with the current development branch:

```bash
git switch feature/postgres-persistence
```

### 2. Configure environment variables

Start with the example environment file:

```bash
cp .env.example .env
```

On Windows Command Prompt:

```cmd
copy .env.example .env
```

Review `.env` and configure the required database, Redis, and application settings for your environment. Do not commit real credentials or production secrets.

### 3. Start the application stack

```bash
docker compose up --build
```

Check the container status:

```bash
docker compose ps
```

Use the service configuration in `docker-compose.yml` and `.env.example` to confirm the actual ports and health checks for your checkout.

### 4. Access the API

Application base URL:

```text
http://localhost:8080
```

Swagger UI:

```text
http://localhost:8080/swagger-ui.html
```

OpenAPI specification:

```text
http://localhost:8080/v3/api-docs
```

The OpenAPI document describes the API endpoints and their request/response schemas. These documentation endpoints are enabled for development and demonstration; secure or disable them as appropriate in production.

## REST API Reference

The API uses the `/api/chatapp/chatrooms` route prefix.

| Operation             | Method | Endpoint                                            |
| --------------------- | ------ | --------------------------------------------------- |
| Create a chat room    | POST   | `/api/chatapp/chatrooms`                            |
| Join a room           | POST   | `/api/chatapp/chatrooms/{roomId}/join`              |
| Send a message        | POST   | `/api/chatapp/chatrooms/{roomId}/messages`          |
| Retrieve chat history | GET    | `/api/chatapp/chatrooms/{roomId}/messages?limit=10` |

### Create a chat room

**Request**

```http
POST /api/chatapp/chatrooms
Content-Type: application/json
```

```json
{
  "roomName": "engineering"
}
```

### Join a room

**Request**

```http
POST /api/chatapp/chatrooms/engineering/join
Content-Type: application/json
```

```json
{
  "participant": "Mohit"
}
```

### Send a message

**Request**

```http
POST /api/chatapp/chatrooms/engineering/messages
Content-Type: application/json
```

```json
{
  "participant": "Mohit",
  "message": "Hello from the chat backend"
}
```

### Retrieve chat history

```http
GET /api/chatapp/chatrooms/engineering/messages?limit=10
```

For exact response schemas, status codes, and endpoint descriptions, use Swagger UI or the generated OpenAPI specification.

## Testing

Run the automated test suite using the Maven Wrapper.

**Windows**

```powershell
.\mvnw.cmd clean test
```

**Linux / macOS / Git Bash**

```bash
./mvnw clean test
```

The project includes Spring Boot testing dependencies and Testcontainers support for PostgreSQL integration tests. Docker must be available when tests require containers.

The Surefire configuration sets the test JVM timezone to UTC to help keep timezone-dependent tests consistent.

## Load Testing

The `load-test/` directory contains the k6 load-testing assets and setup notes.

See [`load-test/README.md`](load-test/README.md) for instructions and the recorded benchmark results.

Use those recorded results when evaluating throughput and latency; results can vary with hardware, concurrency, configuration, and environment.

## Screenshots

### Swagger UI

![Swagger UI](docs/screenshots/swagger-ui.png)

The screenshot illustrates the API documentation interface generated by springdoc OpenAPI.

For the complete set of project screenshots and architecture assets, see [`docs/`](docs/).

## Continuous Integration

GitHub Actions runs the project's CI workflow, including build and test steps with PostgreSQL and Redis services.

The development branch has successful workflow runs for:

* CI setup and automated tests
* k6 load-test assets
* OpenAPI and Swagger UI changes
* Swagger UI screenshot

[View workflow runs](https://github.com/MohitKundu360/real-time-chat-backend/actions)

## Current Limitations

* No authentication or authorization is currently implemented.
* A dedicated WebSocket/STOMP client is not included.
* Redis Pub/Sub does not provide message acknowledgements or durable delivery.
* Database schema evolution should move to a versioned migration tool as the project matures.
* Swagger UI and OpenAPI endpoints should be secured or disabled in production.
* Load-test results are environment-dependent and should not be treated as universal performance guarantees.

## Future Improvements

* Add JWT-based authentication and room-level authorization.
* Implement WebSocket/STOMP support for connected chat clients.
* Introduce database migrations with Flyway or Liquibase.
* Improve message-delivery guarantees and recovery behavior.
* Add metrics, structured logging, and distributed tracing.
* Expand load testing and document reproducible performance measurements.

## Author

**Mohit Kundu**

Backend Software Engineer

* [GitHub](https://github.com/Mohitkundu360)
* [LinkedIn](https://linkedin.com/in/mohitkundu-backend)
* [Portfolio](https://portfolio-steel-mu-35.vercel.app)

## License

This project is licensed under the MIT License. See [`LICENSE`](LICENSE).
