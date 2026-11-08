# company-session3

Startprosjekt for Spring Boot-kurset, økt 3: konfigurasjon og profiler.

## Oppsett

- JDK 25, en IDE (IntelliJ eller VS Code med Java-utvidelsene) og Postman.
- Lag din egen branch. Navnet har øktnummer, så det ikke kolliderer med andre økter:

```
git checkout -b my-answers-3 session3-start
```

- Åpne mappen `session-3/` (ikke repo-roten) i IntelliJ og vent til Maven-synken er ferdig.
- Kjør `CompanyApplication` med den grønne ▶-knappen ved `main`-metoden.
  Alternativt fra terminalen: `mvn spring-boot:run`.
- Åpne <http://localhost:8080/actuator/health>. Du skal se `{"status":"UP"}`.
- I Postman: importer alt i `postman/` og velg miljøet «Brights local».

## Nyttige verktøy i økta

- Run configuration i IntelliJ: *Active profiles*, *Environment variables* og *Program arguments*
- Actuator: <http://localhost:8080/actuator/env> og <http://localhost:8080/actuator/configprops>
- Postman-samlingen «Spring Boot course: session 3»
- Oppstartsloggen: linjen `The following 1 profile is active: ...` viser hvilken profil du kjører med

## Tags

| Tag | Innhold |
|---|---|
| `session3-start` | Prosjektet slik deltakerne får det |
| `session3-demo-done` | Start pluss demokoden |
| `session3-solution` | Alle oppgaver løst, med tester (bare foreleser) |

Bytt tag: `git checkout session3-start`

Oppgavene er merket `TODO 3.x` i koden. Stubben for den nye innstillingsklassen ligger i `no.course.company.tasks`.
Økt 2 er grunnlaget for prosjektet: CRUD-endepunktene, valideringen og `ApiExceptionHandler` er løsningen derfra (uten tidtakingsfilteret).
