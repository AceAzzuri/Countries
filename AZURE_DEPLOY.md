# Azure MVP deploy

Use this setup for a simple 1-2 month MVP deploy.

## Azure services

- Azure App Service for Containers
- Azure Database for PostgreSQL

## Required App Service settings

Set these as environment variables in Azure App Service:

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

Use a non-guessable admin username for public demos. Move to real authentication before sustained public traffic.

## Build

The included `Dockerfile` builds the Spring Boot jar and runs it with the `prod` profile.
