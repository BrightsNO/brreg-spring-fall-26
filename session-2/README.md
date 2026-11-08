# company-session2

Startprosjekt for Spring Boot-kurset, økt 2: veien til en forespørsel, CRUD og validering.

## Oppsett

- JDK 25, en IDE (IntelliJ eller VS Code med Java-utvidelsene) og Postman.
- Klon repoet og lag din egen branch:

```
git checkout -b my-answers session2-start
```

- Åpne mappen i IntelliJ og vent til Maven-synken er ferdig.
- Kjør `CompanyApplication` med den grønne ▶-knappen ved `main`-metoden.
  Alternativt fra terminalen: `mvn spring-boot:run` (krever Maven på PATH).
- Åpne <http://localhost:8080/actuator/health>. Du skal se `{"status":"UP"}`.
- I Postman: importer alt i `postman/` (samlingen og miljøet), og velg miljøet «Brights local».

## Nyttige verktøy i økta

- Debugger (breakpoint i `CompanyController`)
- Actuator: <http://localhost:8080/actuator/mappings>
- Postman-samlingen «Spring Boot course: session 2»

## Tags

| Tag | Innhold |
|---|---|
| `session2-start` | Prosjektet slik deltakerne får det |
| `session2-demo-done` | Start pluss demokoden |
| `session2-solution` | Alle oppgaver løst, med tester (bare foreleser) |

Bytt tag: `git checkout session2-start`

Oppgavene er merket `TODO 2.x` i koden. Stubber for nye klasser ligger i pakken `no.course.company.tasks`.
Økt 1 er grunnlaget for prosjektet: `OrgNumberValidator`, `CompanyService` og de to varslingsklassene er de du kjenner fra før.
