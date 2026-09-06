# DevFlow – Project Management & Team Collaboration Platform

[![CI Pipeline](https://github.com/Nitish115/devflow/actions/workflows/ci.yml/badge.svg)](https://github.com/Nitish115/devflow/actions/workflows/ci.yml)

DevFlow is an enterprise-ready Project Management and Team Collaboration backend application built with Java 21 and Spring Boot.

---

## Continuous Integration (CI)

DevFlow uses **GitHub Actions** for automated Continuous Integration on every push to `main` and pull request targeting `main`.

- **Test Automation Job (`test`)**: Spins up JDK 21 Temurin, caches Maven dependencies, initializes GitHub Actions service containers (`redis:7-alpine` and `rabbitmq:3-management-alpine`), and executes `./mvnw test` (including Testcontainers-backed integration tests).
- **Docker Build Verification Job (`docker-build`)**: Verifies multi-stage `Dockerfile` compilation cleanly on `ubuntu-latest`.
- **Diagnostic Artifacts**: If any test fails, Surefire test execution reports are automatically uploaded as job artifacts in GitHub Actions for debugging.

---

## Getting Started & Running the Application

### Option A: Running with Docker Compose (Recommended)

The entire application stack (Spring Boot API, MySQL 8.0, RabbitMQ 3, and Redis 7) can be spun up with a single command.

#### Prerequisites
- [Docker Desktop](https://www.docker.com/products/docker-desktop/) installed and running.
- **Port Availability**: Ensure local standalone instances of MySQL (`3306`), RabbitMQ (`5672`, `15672`), and Redis (`6379`) are stopped to prevent port binding conflicts.

#### Steps
1. **Configure Environment Variables**:
   Copy `.env.example` to `.env`:
   ```bash
   cp .env.example .env
   ```
   *(On Windows PowerShell: `copy .env.example .env`)*

   Review `.env` and customize `JWT_SECRET` or database/messaging credentials if desired.

2. **Build & Start the Stack**:
   ```bash
   docker compose up --build -d
   ```

3. **Verify Service Health & Logs**:
   ```bash
   docker compose ps
   docker compose logs -f app
   ```

4. **Access Applications & Services**:
   - **DevFlow REST API**: `http://localhost:8081`
   - **RabbitMQ Management Dashboard**: `http://localhost:15672` (User: `guest`, Password: `guest`)
   - **MySQL Database**: `localhost:3306` (User: `root`, DB: `devflow_db`)
   - **Redis Cache**: `localhost:6379`

5. **Stop the Stack**:
   To stop containers:
   ```bash
   docker compose down
   ```
   To stop containers and remove persistent data volumes (`mysql_data`, `rabbitmq_data`):
   ```bash
   docker compose down -v
   ```

---

### Option B: Local Non-Docker Development

If you prefer running the Spring Boot application locally against standalone database and message broker services:

#### Prerequisites
- **Java 21 JDK** installed (`java -version`).
- Local **MySQL 8** instance running on port `3306` with database `devflow_db`.
- Local **RabbitMQ** instance running on port `5672`.
- Local **Redis** instance running on port `6379`.

#### Running the Application
1. Start your local MySQL, RabbitMQ, and Redis services.
2. Run the Spring Boot application using the Maven Wrapper:
   - **Linux/macOS**:
     ```bash
     ./mvnw spring-boot:run
     ```
   - **Windows**:
     ```powershell
     .\mvnw.cmd spring-boot:run
     ```
3. The API will be accessible at `http://localhost:8081`.

#### Running Tests
To run unit and integration tests (uses Testcontainers for integration testing):
```bash
./mvnw test
```
*(Windows: `.\mvnw.cmd test`)*
