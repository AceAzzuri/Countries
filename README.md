# Copa Mundial Arena MVP

[![Java](https://img.shields.io/badge/Java-17-blue)](#tech-stack)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.3-brightgreen)](#tech-stack)
[![Maven](https://img.shields.io/badge/Maven-wrapper-orange)](#installation)
[![Docker](https://img.shields.io/badge/Docker-ready-blue)](#docker)
[![Tests](https://img.shields.io/badge/tests-45%20passing-brightgreen)](#testing)
[![License: MIT](https://img.shields.io/badge/License-MIT-yellow.svg)](LICENSE)

Standalone Spring Boot MVP for Copa Mundial 2026 Arena score predictions.

## Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Tech Stack](#tech-stack)
- [Architecture](#architecture)
- [Installation](#installation)
- [Running Locally](#running-locally)
- [Docker](#docker)
- [Azure Deployment](#azure-deployment)
- [Testing](#testing)
- [Project Structure](#project-structure)
- [Configuration](#configuration)
- [Security](#security)
- [Scope](#scope)
- [Screenshots](#screenshots)
- [Deployment Checklist](#deployment-checklist)

## Overview

Copa Mundial Arena is a focused MVP for World Cup 2026 score predictions. The app provides a simple mobile-friendly experience for users to log in, submit predictions, view a leaderboard, manage reminder preferences, and access basic information and privacy pages.

The project is structured as a standalone Spring Boot application in the repository root.

## Features

- Copa Mundial 2026 Arena score predictions
- Leaderboard
- Simple username login now, Google login later
- Reminder preferences
- PWA-style mobile experience
- Admin results page
- Consent export endpoint for users who accepted Arena messages

## Tech Stack

- Java 17
- Spring Boot 4.0.3
- Spring MVC
- Spring Security
- Spring Data JPA
- Thymeleaf
- Maven Wrapper
- H2 for quick local development
- PostgreSQL for production-like local setup and deployment
- Docker

## Architecture

The application follows a conventional Spring Boot MVC structure:

- Controllers handle web routes and form submissions.
- Services contain application behavior such as scoring, leaderboards, reminders, predictions, and data collection.
- Repositories use Spring Data JPA for persistence.
- Thymeleaf templates render server-side pages.
- Static assets live under `src/main/resources/static`.
- Environment-specific configuration is handled through Spring profiles and environment variables.
- Spring Security provides CSRF protection for form submissions while preserving the MVP's simple username-based login flow.

Default local execution uses file-based H2. The `prod` profile switches persistence to PostgreSQL and disables the H2 console.

## Installation

Prerequisites:

- Java 17
- Docker, if running PostgreSQL locally or building the container image

The project uses the Maven Wrapper, so a separate Maven installation is not required.

Clone the repository and enter the project root:

```bash
git clone https://github.com/AceAzzuri/Copa-Mundial-Arena.git
cd Copa-Mundial-Arena
```

## Running Locally

For quick local preview without PostgreSQL:

```bash
./mvnw spring-boot:run
```

The app defaults to:

- URL: `http://localhost:8088`
- Database: `jdbc:h2:file:${BALLERS_CLUB_DATA_DIR:${user.home}/.ballers-club/data}/ballers-club-local`
- H2 console: `http://localhost:8088/h2-console`

For a production-like local setup, start PostgreSQL:

```bash
docker compose up -d
```

Then run the app with the `prod` profile:

```bash
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod
```

The `prod` profile defaults to:

- Database: `jdbc:postgresql://localhost:5432/ballers_club`
- User/password: `ballers_club` / `ballers_club`

Build and run the packaged JAR:

```bash
./mvnw clean package
java -jar target/ballers-club-0.0.1-SNAPSHOT.jar
```

## Docker

The repository includes:

- `Dockerfile` for building and running the Spring Boot application with the `prod` profile
- `docker-compose.yml` for a local PostgreSQL database

Start PostgreSQL locally:

```bash
docker compose up -d
```

The Docker image builds the application with:

```bash
mvn -B package -DskipTests
```

and runs:

```bash
java -jar /app/app.jar
```

The container exposes port `8080` and sets:

- `SPRING_PROFILES_ACTIVE=prod`
- `PORT=8080`

## Azure Deployment

Use this setup for a simple 1-2 month MVP deploy.

Azure services:

- Azure App Service for Containers
- Azure Database for PostgreSQL

Required App Service settings:

```text
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://<host>:5432/<database>?sslmode=require
DATABASE_USERNAME=<database-user>
DATABASE_PASSWORD=<database-password>
PORT=8080
COOKIE_SECURE=true
BALLERS_CLUB_ADMIN_USERNAMES=<admin-usernames>
DDL_AUTO=update
```

`DDL_AUTO=update` is acceptable for the short MVP period. Move to database migrations before heavier public traffic.

Production can override with:

- `PORT`
- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `DDL_AUTO`
- `COOKIE_SECURE`
- `BALLERS_CLUB_ADMIN_USERNAMES` via `ballers-club.admin-usernames` if your host maps env vars to Spring properties

The included `Dockerfile` builds the Spring Boot jar and runs it with the `prod` profile.

## Testing

Run the test suite:

```bash
./mvnw test
```

Current verified result:

```text
Tests run: 45, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

## Project Structure

```text
Copa-Mundial-Arena/
├── .github/workflows/        # GitHub Actions workflow configuration
├── .mvn/wrapper/             # Maven Wrapper configuration
├── docs/                     # Project documentation and screenshot placeholders
├── docs/architecture.md      # Architecture notes
├── src/main/java/            # Spring Boot application code
├── src/main/resources/       # Templates, static assets, and application config
├── src/test/                 # Automated tests
├── AZURE_DEPLOY.md           # Azure MVP deployment notes
├── CHANGELOG.md              # Release notes
├── Dockerfile                # Container build for the application
├── LICENSE                   # MIT license
├── SECURITY.md               # Security policy and production notes
├── docker-compose.yml        # Local PostgreSQL service
├── pom.xml                   # Maven project definition
├── mvnw                      # Maven Wrapper for macOS/Linux
├── mvnw.cmd                  # Maven Wrapper for Windows
└── README.md                 # Project documentation
```

## Configuration

Runtime configuration is supplied through Spring properties and environment variables.

```text
SPRING_PROFILES_ACTIVE=prod
DATABASE_URL=jdbc:postgresql://<host>:5432/<database>?sslmode=require
DATABASE_USERNAME=<database-user>
DATABASE_PASSWORD=<database-password>
PORT=8080
COOKIE_SECURE=true
BALLERS_CLUB_ADMIN_USERNAMES=<admin-usernames>
DDL_AUTO=update
```

Use `.env.example` as a local template. Do not commit real secrets.

## Security

- CSRF protection is enabled for form POSTs.
- The H2 console is enabled only in local/default configuration and disabled in the `prod` profile.
- Error responses hide stack traces and messages.
- Admin access is controlled by `ballers-club.admin-usernames` / `BALLERS_CLUB_ADMIN_USERNAMES`.
- The MVP uses simple username-based login. Use a non-guessable admin username for any public demo, and move to real authentication before sustained public traffic.

See [SECURITY.md](SECURITY.md) for reporting and production notes.

## Scope

Included:

- `/`
- `/arena`
- `/leaderboard`
- `/login`
- `/settings`
- `/info`
- `/privacy`
- `/admin/results`
- `/admin/consent-export.csv`

Excluded by design:

- Additional products and universes
- chat
- full social network
- native app
- advanced admin system

## Screenshots

Add current screenshots before sharing this as a portfolio project:

```text
docs/screenshots/
├── 01-home.png
├── 02-arena.png
├── 03-leaderboard.png
├── 04-login.png
└── 05-admin-results.png
```

Suggested README placements:

```markdown
![Home screen](docs/screenshots/01-home.png)
![Arena predictions](docs/screenshots/02-arena.png)
![Leaderboard](docs/screenshots/03-leaderboard.png)
```

## Deployment Checklist

Before public launch:

- Run with the `prod` profile and PostgreSQL, not local H2.
- Set `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `PORT`.
- Keep `COOKIE_SECURE=true` behind HTTPS.
- Set real admin usernames with `ballers-club.admin-usernames`.
- Decide whether `DDL_AUTO=update` is acceptable for first MVP deploy; move toward migrations before serious production traffic.
- Add a real contact email to `/privacy` before collecting public users.
- Use `/admin/consent-export.csv` only for users who accepted Arena messages.
- Keep email volume low: one important reminder and final placement/prize message.
