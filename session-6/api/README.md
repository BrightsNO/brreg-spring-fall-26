# api (økt 6)

Company-API-et i økt 6. Det er her du jobber. Oppsett, brukere, klienter og tags står i
[README-en i rotmappen](../README.md).

## Kjøre API-et

- Start autorisasjonsserveren først (`AuthServerApplication` i `auth-server/`), se README-en i rotmappen.
- Kjør `CompanyApplication` med den grønne ▶-knappen ved `main`-metoden.
  Alternativt fra rotmappen i terminalen: `mvn -f api/pom.xml spring-boot:run`.
- `GET http://localhost:8080/api/companies` uten token gir `401`.

## Hvor ligger hva

| Pakke | Innhold |
|---|---|
| `no.course.company.tasks` | `SecurityConfig` (oppgave 6.3 og 6.4) |
| `no.course.company.legacy` | Koden fra økt 5: egen innlogging og egne tokens. Oppgave 6.3 sletter den. |
| `no.course.company.security` | Gitt, fra økt 4: `FilterChainPrinter`, `RequestLoggingFilter`, `CorsConfig` |

## Nyttige verktøy

- Postman. Importer `postman/session6.postman_collection.json`.
- TRACE-logg fra Spring Security: start med profilen `trace` i *Active profiles* i run configuration
  (eller `mvn -f api/pom.xml spring-boot:run -Dspring-boot.run.profiles=trace` fra rotmappen).
