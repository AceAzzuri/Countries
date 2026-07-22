# Security Policy

## Supported Versions

This repository is an MVP portfolio project. Security fixes are applied to the
current `main` branch.

## Reporting a Vulnerability

Please do not open public issues for vulnerabilities that expose user data,
deployment credentials, or administrative access.

Report security concerns privately to the repository owner.

## Production Notes

- Configure `SPRING_PROFILES_ACTIVE=prod` for deployed environments.
- Set database credentials through environment variables or platform secrets.
- Keep `COOKIE_SECURE=true` behind HTTPS.
- Set `BALLERS_CLUB_ADMIN_USERNAMES` explicitly for each deployment.
- Move from `DDL_AUTO=update` to database migrations before sustained public traffic.
