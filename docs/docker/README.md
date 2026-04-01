# Docker and Deployment - Billard-Book

## Goals

- Deploy the API (and optionally the client) quickly with Docker
- Support iterative development workflows
- Standardize script usage
- Prepare for future extension (DB, monitoring, production hardening)

## Quick Start (API only)

Direct method without docker-compose (recommended now):

```bash
# Build multi-stage image (context = server/, lightweight)
docker build -t monuser/billard-book-api:dev server/

# Run locally
docker run --rm -p 8080:8080 monuser/billard-book-api:dev

# Check health (in another terminal)
curl -f http://localhost:8080/actuator/health
```

Build + health test + push automation script: `scripts/docker/build-push.sh`.

## Deploying to Docker Hub (Detailed Steps)

### 1. Choose and fix a version
Ideally, keep it aligned with Maven versioning (`pom.xml`). Example: `0.1.0`.

### 2. Log in to the registry (once per session)
```bash
docker login -u <dockerhub_user>
```

### 3. Build + health test + push (script)
```bash
./scripts/docker/build-push.sh <dockerhub_user> 0.1.0
```
The script:
- builds the image with two tags: `:0.1.0` and `:latest`
- runs an ephemeral container and waits for `/actuator/health` to become UP
- pushes both tags to Docker Hub

### 4. Registry-side verification
```bash
docker pull <dockerhub_user>/billard-book-api:0.1.0
docker run --rm -p 8080:8080 <dockerhub_user>/billard-book-api:0.1.0
curl -f http://localhost:8080/actuator/health
```

### 5. Run with environment variables
```bash
docker run \
  --rm -p 9090:8080 \
  -e PORT=8080 \
  -e SPRING_PROFILES_ACTIVE=docker \
  -e JWT_EXPIRATION_MS=3600000 \
  <dockerhub_user>/billard-book-api:0.1.0
```

### 6. (Optional) Multi-architecture build
Useful when target platform (for example, Raspberry Pi) differs from your machine:
```bash
docker buildx build --platform linux/amd64,linux/arm64 \
  -t <dockerhub_user>/billard-book-api:0.1.0 \
  -t <dockerhub_user>/billard-book-api:latest \
  --push server/
```

### 7. (Optional) Versioning policy
- `MAJEUR.MINOR.PATCH` (ex: 0.1.0)
- Reserve `latest` for the newest stable release
- Add extra tags (for example, `0.1.0-build12`) in CI when needed

### 8. (Optional) Render / other PaaS integration
In Render, choose "Deploy an existing image" and set `docker.io/<dockerhub_user>/billard-book-api:0.1.0`, then define `PORT` if required. No Dockerfile is needed on Render side.

### 9. Minimal recap
```bash
docker login -u monuser
./scripts/docker/build-push.sh monuser 0.1.0
docker run --rm -p 8080:8080 monuser/billard-book-api:0.1.0
```

---

## Container Architecture

```text
Host
 ├─ billard-api :8080  (Spring Boot, JWT, Actuator)
 ├─ (optional) nginx :80  (static client + /api proxy)
 └─ (optional) postgres :5432 (future persistence)
```

Network: `billard-network` (bridge).  
Volumes: `logs/` (API), and future `postgres_data`.

## Key Files

| File | Role |
|---------|------|
| `docker-compose.yml` | (Optional / Legacy) Multi-service orchestration for future needs |
| `server/Dockerfile` | Multi‑stage (build Maven + runtime JRE) |
| `nginx.conf` | Proxy + cache + compression (if the client is containerized) |
| `scripts/docker/build-push.sh` | Build + test health + push Docker Hub |
| `scripts/docker/stop.sh` | Stop compose stack (optional if compose is used) |
| `server/.dockerignore` | Build context reduction |
| `server/src/main/resources/application-docker.properties` | Spring Docker profile |

## Dockerfile (multi-stage)

- Build stage: Eclipse Temurin JDK 21 + Maven dependency cache
- Runtime stage: JRE 21 Alpine, non-root user
- Benefits: compact image, reduced attack surface, effective layer caching

## Health and Monitoring

Actuator endpoint documented in OpenAPI:

```text
GET /actuator/health -> { status: "UP", groups: ["liveness","readiness"] }
```

Compose healthcheck can run on a 30s interval with restart policies.

## Baseline Security

- Non-root user in final image
- Private Docker network (avoid exposing internal ports unnecessarily)
- Future option: HTTPS via nginx + certbot (not included yet)

## Environment Variables (examples)

In `docker-compose.yml`:

```text
SPRING_PROFILES_ACTIVE=docker
JAVA_OPTS=-Xmx512m -Xms256m
JWT_EXPIRATION_MS=3600000
```

Adjust memory settings based on target host capacity.

## Minimal Workflow

| Need | Command |
|--------|----------|
| Build local | `docker build -t monuser/billard-book-api:0.1.0 server/` |
| Test local | `docker run --rm -p 8080:8080 monuser/billard-book-api:0.1.0` |
| Health check | `curl -f http://localhost:8080/actuator/health` |
| Build + push (script) | `./scripts/docker/build-push.sh monuser 0.1.0` |
| Pull & retest | `docker pull monuser/billard-book-api:0.1.0` |

Legacy scripts (deploy/dev/status/start-daemon/setup) were removed to keep the project lean.

## Scripts

Each script includes a standard header with description and usage. All scripts assume Docker is available and running.

## Debug and Logs

```bash
# Logs API
docker compose logs -f billard-api
# One-time output (last lines only)
docker compose logs --tail=100 billard-api
# Inspect health
curl -s http://localhost:8080/actuator/health | jq
```

## Development Mode

Fast iteration is recommended outside a container:

```bash
cd server
mvn spring-boot:run -Dspring-boot.run.profiles=docker
```

To validate container behavior, rebuild on demand (see workflow above). Container auto-reload is no longer provided.

## Production Roadmap

1. Add Postgres + migrations
2. Add CI (GitHub Actions) for build/push on tags
3. Add observability (metrics + centralized logs)
4. Add reverse proxy hardening (TLS, rate limiting)
5. Harden image strategy (distroless/slim runtime)

## Future DB Extension Example

```yaml
postgres:
  image: postgres:15-alpine
  environment:
    POSTGRES_DB: billardbook
    POSTGRES_USER: billard_user
    POSTGRES_PASSWORD: change_me
  volumes:
    - postgres_data:/var/lib/postgresql/data
  networks:
    - billard-network
```

## Cleanup

```bash
# Container stops automatically (run --rm)
# Remove unused images
docker image prune -f
# Global cleanup (destructive):
docker system prune -f
```

## Quick FAQ

| Problem | Suggestion |
|----------|-------|
| Health DOWN | Check API logs and verify port 8080 is not already used |
| Slow rebuild | Review `.dockerignore` and Maven dependency caching |
| No auto reload | Use `mvn spring-boot:run` for development |
| Log permission issues | Verify runtime UID and permissions on `logs` |

