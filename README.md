<img width="853" height="489" alt="Screenshot 2026-09-06 at 7 24 30 PM" src="https://github.com/user-attachments/assets/455f2739-c78a-4de3-861c-53770ff5db65" />
# RateGuard

Distributed rate limiter using Spring Boot and Redis. Three different algorithms, three endpoints, so you can actually compare how they behave instead of just picking one.

Built with Java 21, Spring Boot 3.3.5, Redis 7, Docker.

## What it does

You send a client ID to an endpoint, it checks Redis, and tells you if the request is allowed or blocked.

Three algorithms are implemented:

| Algorithm | Redis structure | Notes |
|---|---|---|
| Fixed Window Counter | Key + `INCR` | Easiest to implement, but bursts can slip through at window edges |
| Token Bucket | Hash (tokens, last refill time) | Allows short bursts without being unfair, closer to what real APIs use |
| Sliding Window Log | Sorted Set (`ZADD`/`ZRANGE`) | Most accurate, but stores every request timestamp so memory usage grows |

## Tech stack

- Java 21
- Spring Boot 3.3.5 (Web, Data Redis, Validation)
- Redis 7 (Alpine)
- Docker / Docker Compose

## Folder structure

~~~
rateguard/
├── pom.xml
├── Dockerfile
├── docker-compose.yml
├── LoadTest.java
└── src/main/java/com/rateguard/
    ├── RateguardApplication.java
    ├── config/
    │   └── RedisConfig.java
    ├── dto/
    │   └── RateLimitResponse.java
    ├── service/
    │   ├── FixedWindowRateLimiter.java
    │   ├── TokenBucketRateLimiter.java
    │   └── SlidingWindowRateLimiter.java
    ├── controller/
    │   └── RateLimitController.java
    └── exception/
        └── GlobalExceptionHandler.java
└── src/main/resources/
    └── application.properties
~~~

## Running it

Start Redis:

~~~bash
docker run -d --name redis-local -p 6379:6379 redis:7-alpine
~~~

Run the app:

~~~bash
mvn spring-boot:run
~~~

Runs on port 8090.

Test:

~~~bash
curl -X POST "http://localhost:8090/api/rate-limit/fixed-window/check?clientId=abhi"
curl -X POST "http://localhost:8090/api/rate-limit/token-bucket/check?clientId=abhi"
curl -X POST "http://localhost:8090/api/rate-limit/sliding-window/check?clientId=abhi"
~~~

## Endpoints

| Method | Endpoint | Description |
|---|---|---|
| POST | `/api/rate-limit/fixed-window/check?clientId={id}` | Fixed Window check |
| POST | `/api/rate-limit/token-bucket/check?clientId={id}` | Token Bucket check |
| POST | `/api/rate-limit/sliding-window/check?clientId={id}` | Sliding Window check |

Response:

~~~json
{
  "allowed": true,
  "remaining": 9,
  "retryAfterSeconds": 0
}
~~~

Blocked requests return HTTP 429.

## Load testing

~~~bash
javac LoadTest.java
java LoadTest
~~~

Fires 20 concurrent requests from separate threads to check the limiter holds up under concurrency.

<img width="853" height="489" alt="Screenshot 2026-09-06 at 7 24 30 PM" src="https://github.com/user-attachments/assets/455f2739-c78a-4de3-861c-53770ff5db65" />

## Docker

~~~bash
mvn clean package -DskipTests
docker-compose up --build
~~~

## License

MIT
