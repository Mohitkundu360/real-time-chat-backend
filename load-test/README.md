# Load test (k6)

Real measurements for the chat API. Indicative only, not a benchmark.

## What it does
`chat.js` creates 10 rooms. Each virtual user (VU) then loops: join once, send one message, read the latest 10 messages, sleep 0.5 to 1.0 s.

Profile: 0 to 50 VUs over 20 s, 50 to 100 over 40 s, 100 to 0 over 20 s (80 s total).

Thresholds: failed requests under 1%, and p95 under 500 ms overall, for send, and for history.

## Environment
- Laptop: Intel Core i5-12450H, 15.7 GB RAM, Windows
- Docker Desktop, VM memory about 7.8 GB
- App: Spring Boot 3.5.3, Java 17 (eclipse-temurin:17-jre), default Tomcat and Hikari settings
- PostgreSQL 16 and Redis 7 in Docker
- k6 (grafana/k6 image) on the same machine as the system under test

## How to run

    docker compose up -d --build
    docker run --rm -i grafana/k6 run -e BASE_URL=http://host.docker.internal:8080 - < load-test/chat.js

## Results (3 runs, same machine, back to back)

| Run | Requests | req/s | Failed | p95 overall | p95 send | p95 history | Checks passed |
|---|---|---|---|---|---|---|---|
| 1 | 11,872 | 147.7 | 0.00% | 14.97 ms | 16.78 ms | 11.58 ms | 17,753 / 17,753 |
| 2 | 11,974 | 149.2 | 0.00% | 11.21 ms | 12.76 ms | 8.18 ms | 17,906 / 17,906 |
| 3 | 11,972 | 148.6 | 0.00% | 10.91 ms | 12.27 ms | 7.89 ms | 17,903 / 17,903 |

All thresholds passed in every run. The median run by overall p95 is run 2. Run 1 was the first run after a fresh container start and the slowest, likely JVM warm-up (not verified). Raw summaries: `run-1-summary.txt`, `run-2-summary.txt`, `run-3-summary.txt`.

## Persistence check
Each iteration sends exactly one message. After the smoke run and the three runs, `chat_messages` held 17,771 rows, equal to the 17,771 completed iterations (27 + 5,881 + 5,932 + 5,931). So every message accepted with HTTP 200 was stored in PostgreSQL.

This checks persistence only. It does not check that Redis Pub/Sub subscribers received each message.

## Limitations
- One machine: the load generator, app, PostgreSQL and Redis compete for the same CPU.
- Light load (about 150 requests/s), so this is not a capacity or breaking-point test.
- Default Hikari pool and Tomcat settings, not tuned.
- Virtual users are not real concurrent users.
