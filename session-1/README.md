# company-session1

Startprosjekt for Spring Boot-kurset, økt 1: DI, beans og oppstart.

## Oppsett

- JDK 25, Maven og en IDE (IntelliJ eller VS Code med Java-utvidelsene).
- Klon repoet og lag din egen branch:

```
git checkout -b my-answers session1-start
```

- Åpne mappen i IntelliJ og vent til Maven-synken er ferdig.
- Kjør `CompanyApplication` med den grønne ▶-knappen ved `main`-metoden.
  Alternativt fra terminalen: `mvn spring-boot:run` (krever Maven på PATH).
- Åpne <http://localhost:8080/actuator/health>. Du skal se `{"status":"UP"}`.

## Nyttige verktøy i økta

- Debugger (breakpoint i konstruktøren til `CompanyService`)
- Actuator: <http://localhost:8080/actuator/beans>
- Conditions report: start med `--debug` i *Program arguments* i run configuration
  (eller `mvn spring-boot:run -Dspring-boot.run.arguments=--debug`)

## Tags

| Tag | Innhold |
|---|---|
| `session1-start` | Prosjektet slik deltakerne får det |
| `session1-demo-done` | Start pluss demoene D1–D3 (minicontainer, BeanCounterRunner, Jetty-blokk i pom.xml) |
| `session1-solution` | Alle oppgaver løst (foreleser) |

Bytt tag: `git checkout session1-start`

Oppgavene ligger i pakken `no.course.company.tasks` som stubber med TODO-kommentarer.
