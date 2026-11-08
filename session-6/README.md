# company-session6

Startprosjekt for Spring Boot-kurset, økt 6: OAuth2 og resource server.
Det består av to applikasjoner:

| Mappe | Hva | Port |
|---|---|---|
| `auth-server/` | En liten autorisasjonsserver (Spring Authorization Server). Gitt: du bare kjører den. | 9000 |
| `api/` | Company-API-et fra økt 5. Det er her du jobber. | 8080 |

API-et starter fra den ferdige tilstanden etter økt 5 (egen innlogging, egne HS256-tokens), så du trenger ikke dine
egne svar fra økt 5.

## Oppsett

- JDK 25 og en IDE (IntelliJ eller VS Code med Java-utvidelsene).
- Postman installert.
- Start begge applikasjonene med den grønne ▶-knappen ved `main`-metoden, først autorisasjonsserveren og så API-et:
  - `AuthServerApplication` (i `auth-server/`)
  - `CompanyApplication` (i `api/`)

  Alternativt fra terminalen, i to separate vinduer:

```
mvn -f auth-server/pom.xml spring-boot:run
```

```
mvn -f api/pom.xml spring-boot:run
```

- Sjekk autorisasjonsserveren: åpne <http://localhost:9000/.well-known/openid-configuration> i nettleseren.
- Sjekk API-et: `GET http://localhost:8080/api/companies` uten token gir `401`.
- Autorisasjonsserveren lager en ny signeringsnøkkel hver gang den starter. Etter en omstart slutter gamle tokens å virke.

## Hvem er hvem

| Navn | Type | Hvordan den logger inn |
|---|---|---|
| `postman` | En klientapp for mennesker (public client, uten hemmelighet) | Authorization Code med PKCE. Scopes `openid`, `companies.read`, `companies.write` |
| `machine` | Et annet system (confidential client) | Client Credentials med hemmeligheten `machine-secret`. Scope `companies.read` |
| `user` / `password` | En person med rollen `USER` | på innloggingssiden til autorisasjonsserveren |
| `admin` / `admin` | En person med rollen `ADMIN` | på innloggingssiden til autorisasjonsserveren |

Tokens varer i 30 minutter på dette kurset.

## Hvor ligger hva (API)

| Pakke | Innhold |
|---|---|
| `no.course.company.tasks` | `SecurityConfig` (oppgave 6.3 og 6.4) |
| `no.course.company.legacy` | Koden fra økt 5: egen innlogging og egne tokens. Oppgave 6.3 sletter den. |
| `no.course.company.security` | Gitt, fra økt 4: `FilterChainPrinter`, `RequestLoggingFilter`, `CorsConfig` |

## Nyttige verktøy i økta

- Postman. Importer `api/postman/session6.postman_collection.json`. Fanen *Authorization* har typen *OAuth 2.0*.
- <https://jwt.io> for å lese et token.
- TRACE-logg fra Spring Security: start API-et med profilen `trace` i *Active profiles* i run configuration
  (eller `mvn -f api/pom.xml spring-boot:run -Dspring-boot.run.profiles=trace`).

## Tags

| Tag | Innhold |
|---|---|
| `session6-start` | Prosjektet slik deltakerne får det |
| `session6-demo-done` | Start pluss demokonfigurasjonene D2–D3 (Spring-profiler) |
| `session6-solution` | Alle oppgaver løst (foreleser) |

Bytt tag: `git checkout session6-start`
