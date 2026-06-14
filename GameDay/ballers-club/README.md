# Copa Mundial Arena MVP

Standalone Spring Boot MVP for the first launch product:

- Copa Mundial 2026 Arena score predictions
- leaderboard
- simple username login now, Google login later
- reminder preferences
- PWA-style mobile experience

## Run locally

For quick local preview without PostgreSQL:

```bash
cd GameDay/ballers-club
./mvnw spring-boot:run
```

Start PostgreSQL for a production-like local setup:

```bash
docker compose up -d
```

Run the app from the repository root:

```bash
GameDay/ballers-club/mvnw -f GameDay/ballers-club/pom.xml spring-boot:run -Dspring-boot.run.profiles=prod
```

Or run the packaged JAR after building:

```bash
GameDay/ballers-club/mvnw -f GameDay/ballers-club/pom.xml clean package
java -jar GameDay/ballers-club/target/ballers-club-0.0.1-SNAPSHOT.jar
```

The app defaults to:

- URL: `http://localhost:8088`
- Database: `jdbc:h2:file:${BALLERS_CLUB_DATA_DIR:${user.home}/.ballers-club/data}/ballers-club-local`
- H2 console: `http://localhost:8088/h2-console`

Use the `prod` profile for PostgreSQL:

- Database: `jdbc:postgresql://localhost:5432/ballers_club`
- User/password: `ballers_club` / `ballers_club`

Production can override with:

- `PORT`
- `DATABASE_URL`
- `DATABASE_USERNAME`
- `DATABASE_PASSWORD`
- `DDL_AUTO`
- `COOKIE_SECURE`
- `BALLERS_CLUB_ADMIN_USERNAMES` via `ballers-club.admin-usernames` if your host maps env vars to Spring properties

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

## Deployment checklist

Before public launch:

- Run with the `prod` profile and PostgreSQL, not local H2.
- Set `DATABASE_URL`, `DATABASE_USERNAME`, `DATABASE_PASSWORD`, and `PORT`.
- Keep `COOKIE_SECURE=true` behind HTTPS.
- Set real admin usernames with `ballers-club.admin-usernames`.
- Decide whether `DDL_AUTO=update` is acceptable for first MVP deploy; move toward migrations before serious production traffic.
- Add a real contact email to `/privacy` before collecting public users.
- Use `/admin/consent-export.csv` only for users who accepted Arena messages.
- Keep email volume low: one important reminder and final placement/prize message.

Excluded by design:

- Additional products and universes
- chat
- full social network
- native app
- advanced admin system
