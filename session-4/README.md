# company-session4

Startprosjekt for Spring Boot-kurset, økt 4: grunnleggende Spring Security (filtre, autentisering, CSRF og CORS).
Prosjektet står på egne ben: du trenger ingenting fra de tidligere øktene.

## Oppsett

- JDK 25 og en IDE (IntelliJ eller VS Code med Java-utvidelsene).
- Postman installert.
- Kjør `CompanyApplication` med den grønne ▶-knappen ved `main`-metoden.
  Alternativt fra terminalen: `mvn spring-boot:run`.
- Spring Security ligger allerede på classpath. Uten konfigurasjon låser den hele applikasjonen: `GET /api/companies` gir `401`. Det er forventet, og det er der oppgave 4.1 starter.
- Se etter `Using generated security password: ...` i konsollen. Brukernavnet er `user`.

## Hva som er i prosjektet

| Endepunkt | Formål |
|---|---|
| `GET /public/info` | Skal være åpent for alle |
| `GET /api/companies` og `GET /api/companies/{organizationNumber}` | Les foretak |
| `POST /api/companies` | Registrer et foretak |
| `DELETE /api/companies/{organizationNumber}` | Slett et foretak |
| `GET /api/me` | Viser hvem Spring Security tror du er |
| `GET /actuator/health` | Helsesjekk |

Foretakene ligger i minnet. Det finnes ingen database.

## Nyttige verktøy i økta

- Postman. Importer `postman/session4.postman_collection.json`.
- Debug-logging av Spring Security: start med profilen `trace` i *Active profiles* i run configuration
  (eller `mvn spring-boot:run -Dspring-boot.run.profiles=trace`)
- Utviklerverktøyene i nettleseren (F12), fanene Console og Network, til CORS-oppgaven.
- `tools/cors-test/index.html`: en liten side som kaller API-et fra en annen origin.
  Server den med JDK-ens egen webserver (stopp med Ctrl+C):

```
jwebserver -p 3000 -d tools/cors-test
```

Åpne deretter <http://localhost:3000>.

## Tags

| Tag | Innhold |
|---|---|
| `session4-start` | Prosjektet slik deltakerne får det |
| `session4-demo-done` | Start pluss demokonfigurasjonene D1–D4 (Spring-profiler) |
| `session4-solution` | Alle oppgaver løst (foreleser) |

Bytt tag: `git checkout session4-start`

Oppgavene ligger i pakken `no.course.company.tasks` som stubber med TODO-kommentarer.
