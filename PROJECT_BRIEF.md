# DevFlow – Project Management & Team Collaboration Platform

## Overview
DevFlow is a production-inspired Project Management and Team Collaboration platform built using Java and Spring Boot. It helps organizations manage projects, teams, tasks, files, and notifications in one place while demonstrating enterprise backend development practices.

## Problem Statement
Software teams need a centralized platform to manage projects, tasks, collaboration, files, notifications, and progress tracking.

## Tech Stack
- **Backend:** Java 21, Spring Boot, Spring Security, Spring Data JPA, Hibernate, Maven
- **Database:** MySQL, Flyway
- **Authentication:** JWT, BCrypt
- **Performance:** Redis
- **Messaging:** RabbitMQ
- **File Storage:** MinIO (local), AWS S3 (future)
- **Documentation:** Swagger/OpenAPI
- **Testing:** JUnit 5, Mockito
- **DevOps:** Docker, Docker Compose, GitHub Actions
- **Frontend:** React (Vite) + Vanilla CSS Design System

## User Roles
Organization Owner, Organization Admin, Project Manager, Developer, Viewer.

## Core Features
Authentication, Organization Management, Project Management, Task Management, Comments & Mentions, File Uploads, Notifications, Dashboard.

### RabbitMQ Usage
Used for asynchronous processing. Example: when a task is assigned, the API saves the task and immediately returns a response. RabbitMQ then triggers background consumers to send emails, create notifications, log activities, and update analytics.

### Redis Usage
Used to cache frequently accessed data such as dashboards, users, and projects, reducing database load.

## Database Tables
users, organizations, organization_members, projects, project_members, tasks, comments, attachments, notifications, activity_logs, refresh_tokens.

## Development Roadmap
1. Setup & JWT Authentication [Completed]
2. Organization Management [Completed]
3. Project & Task APIs [Completed]
4. Comments & Attachments [Completed]
5. RabbitMQ Notifications [Completed]
6. Redis Caching [Completed]
7. Testing [Completed]
8. Docker Deployment [Completed]
9. CI/CD [Completed]
10. React Frontend Client [Completed]

## Deployment & Execution
DevFlow supports full containerized orchestration using Docker Compose (Spring Boot + MySQL + RabbitMQ + Redis) as well as standalone local execution for development. For full instructions on setup, environment configuration (`.env`), and commands, refer to [README.md](file:///d:/My%20Projects/Devflow%20Project/README.md).

## Future Enhancements
Sprint Management, Calendar Integration, GitHub Integration, Team Chat, AI Task Suggestions, Time Tracking, Analytics Dashboard.

## Project Goal
Build a production-quality backend application that demonstrates enterprise Java development practices and strengthens a Java Backend Developer portfolio.

---

## Working Agreement (for Antigravity agents)

These rules apply to every task performed in this repository:

1. **Commit after every completed task.** Do not leave work uncommitted when a task is marked done.
2. **Use Conventional Commits** for all commit messages:
   - `feat:` new feature or endpoint
   - `fix:` bug fix
   - `test:` adding or updating tests
   - `chore:` tooling, config, dependency changes
   - `docs:` documentation-only changes
   - `refactor:` code change that neither fixes a bug nor adds a feature
3. **Keep commits scoped.** One logical concern per commit — do not bundle unrelated changes (e.g. don't mix a new endpoint with an unrelated config change).
4. **Show a plan before executing** any task that touches more than a couple of files. Wait for approval before making changes.
5. **Follow the package structure**: separate packages per domain module — `auth`, `organization`, `project`, `task`, `comment`, `attachment`, `notification`, `common` (shared config, exceptions, utils).
6. **No fabricated business logic.** If a requirement is ambiguous, ask rather than inventing scope not listed in this brief.
7. **Write tests alongside features** where practical (JUnit 5 + Mockito), not only in the dedicated "Testing" phase — but it's fine to expand coverage later in Phase 7.
8. **Do not skip ahead in the roadmap.** Build phases in order unless explicitly told otherwise, since later phases (RabbitMQ, Redis) depend on earlier ones (auth, org, task APIs) being stable.
