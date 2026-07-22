# Architecture

Copa Mundial Arena is a standalone Spring Boot MVC application.

## Request Flow

```text
Browser
  -> Spring MVC controller
  -> application service
  -> Spring Data JPA repository
  -> H2 or PostgreSQL
```

Thymeleaf renders server-side HTML pages. Static CSS, JavaScript, images, and
the PWA manifest are served from `src/main/resources/static`.

## Main Areas

- `controller`: web routes, redirects, form handling, and model composition.
- `service`: scoring, predictions, leaderboards, polls, reminders, feedback,
  chat, pools, admin access, and data initialization.
- `repository`: Spring Data JPA persistence boundaries.
- `model`: JPA entities and read models used by templates.
- `templates`: Thymeleaf views.

## Runtime Profiles

- Default profile: file-based H2 for quick local development.
- `prod` profile: PostgreSQL, secure cookies, H2 console disabled.

## Deployment

The repository includes a Dockerfile for Azure App Service for Containers and a
`docker-compose.yml` file for local PostgreSQL.
