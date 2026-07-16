# AlphaECI – Identity Service

Authentication and identity microservice for the AlphaECI platform (Escuela Colombiana de Ingeniería Julio Garavito). Handles OTP-based email verification, JWT login/refresh/logout, and password management. Events are published to RabbitMQ and consumed by the notification microservice.


---

## Quick Start (local development)

```bash
# 1. Copy and fill the environment file
copy .env .env.local   # edit with your local values

# 2. Start everything (first build takes ~2 min)
docker compose up --build

# 3. Stop
docker compose down
```

| Service    | URL |
|------------|-----|
| API        | http://localhost:8080 |
| Swagger UI | http://localhost:8080/swagger-ui.html |
| Health     | http://localhost:8080/actuator/health |
| RabbitMQ   | http://localhost:15672  (guest / guest) |
| SonarQube  | http://localhost:9000 |

> OTP codes and password reset codes are printed to the console log if RabbitMQ is unreachable.
