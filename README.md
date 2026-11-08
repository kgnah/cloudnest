# CloudNest

A mini Platform-as-a-Service built with Spring Boot and Docker for deploying
and managing containerized applications, with GitHub webhook-based automatic
redeployment.

## Features
- JWT authentication (register / login)
- Create, start, stop and delete Docker container deployments via REST API
- Build and deploy directly from a GitHub repository
- GitHub webhook integration with HMAC SHA-256 signature verification
- Automatic rebuild and redeploy on push to `main`
- Scheduled start/stop actions for deployments

## Tech stack
Java 17, Spring Boot, Spring Security (JWT), Spring Data JPA, PostgreSQL,
Docker (docker-java), JGit, ngrok (local webhook testing)

## How it works
```
GitHub push -> Webhook -> CloudNest (verify signature) -> find deployments
-> rebuild image -> redeploy container
```

## Run locally
1. Start PostgreSQL and create a database named `cloudnest`.
2. Set `spring.datasource.password`, `jwt.secret` (base64, 32 bytes) and
   the `GITHUB_WEBHOOK_SECRET` environment variable.
3. Make sure Docker is running.
4. `./mvnw spring-boot:run`

## Known limitations / roadmap
- Host port can change after redeployment (stable ports in progress)
- Rollback and HTTP health checks being improved
- Automated tests and CI to be added
