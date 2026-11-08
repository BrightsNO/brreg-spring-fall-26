# company-session5

Startprosjekt for Spring Boot-kurset, økt 5: tilstandsløs innlogging med JWT.
Prosjektet starter fra den ferdige tilstanden etter økt 4 (HTTP Basic, egen `SecurityFilterChain`, BCrypt-brukere, tilstandsløst, CORS),
så du trenger ikke dine egne svar fra økt 4.

## Oppsett

- JDK 25 og en IDE (IntelliJ eller VS Code med Java-utvidelsene).
- Postman installert.
- Kjør `CompanyApplication` med den grønne ▶-knappen ved `main`-metoden.
  Alternativt fra terminalen: `mvn spring-boot:run`.
- API-et bruker fortsatt HTTP Basic: `GET /api/companies` uten innlogging gir `401`. Det er der økt 5 starter.
- `POST /auth/login` finnes, men gir `500` til du har skrevet `TokenService` (oppgave 5.1).

## Hva som er i prosjektet

| Endepunkt | Formål |
|---|---|
| `POST /auth/login` | Nytt: brukernavn og passord inn, et signert token ut (oppgave 5.1) |
| `GET /public/info` | Åpent for alle |
| `GET /api/companies` og `GET /api/companies/{organizationNumber}` | Les foretak |
| `POST /api/companies` | Registrer et foretak (rolle `ADMIN`) |
| `DELETE /api/companies/{organizationNumber}` | Slett et foretak (rolle `ADMIN`) |
| `GET /api/me` | Viser hvem Spring Security tror du er, og med hvilken type `Authentication` |
| `GET /actuator/health` | Helsesjekk |

Brukere: `user` / `password` (rolle `USER`) og `admin` / `admin` (rolle `ADMIN`).
Foretakene ligger i minnet. Det finnes ingen database.

## Hvor ligger hva

| Pakke | Innhold |
|---|---|
| `no.course.company.tasks` | De to klassene du jobber i: `TokenService` (5.1) og `SecurityConfig` (5.2, 5.3) |
| `no.course.company.auth` | Gitt: `AuthController` (/auth/login), `AuthConfig` (brukere, `JwtEncoder`, `JwtDecoder`), `JwtProperties` |
| `no.course.company.security` | Gitt, fra økt 4: `FilterChainPrinter`, `RequestLoggingFilter`, `CorsConfig` |

Signeringshemmeligheten ligger i `application.yml` (`app.jwt.secret`). Det er greit på et kurs, men er nettopp det du ikke skal gjøre i et ekte prosjekt.

## Nyttige verktøy i økta

- Postman. Importer `postman/session5.postman_collection.json`.
- <https://jwt.io> for å lese et token.
- TRACE-logg fra Spring Security: start med profilen `trace` i *Active profiles* i run configuration
  (eller `mvn spring-boot:run -Dspring-boot.run.profiles=trace`).

## Tags

| Tag | Innhold |
|---|---|
| `session5-start` | Prosjektet slik deltakerne får det |
| `session5-demo-done` | Start pluss demokonfigurasjonene D2–D4 (Spring-profiler) |
| `session5-solution` | Alle oppgaver løst (foreleser) |

Bytt tag: `git checkout session5-start`
