# Økt 2: oppgaver

Du skal bruke forespørselsløpet selv: finn veien gjennom det, bygg de manglende endepunktene, og få API-et til å si nei på en nyttig måte. Hver oppgave har **Mål** (hva), **Hvorfor** (hva du lærer), **Slik gjør du det** og **Ferdig når**.

Dokumentet har to typer innhold:
- Vanlig tekst er oppgaveteksten deltakerne får.
- Bokser merket **🔎 For validering** er for deg som validerer oppsettet. De fjernes før oppgavene deles ut.

Oppgave 2.6 er for dem som blir ferdige tidlig.

---

## Før du begynner

1. Klon repoet og sjekk ut `session2-start`:

   ```
   git checkout -b my-answers session2-start
   ```

   **Hvorfor en egen branch:** du kommer til å endre kode, og vil kunne sammenligne med fasiten uten å miste noe.
2. Start `CompanyApplication`. Åpne <http://localhost:8080/actuator/health>. Du skal se `{"status":"UP"}`.
3. Importer filene i `postman/` til Postman (samlingen «Spring Boot course: session 2» og miljøet «Brights local») og velg miljøet. Samlingen har forespørslene du trenger for hver oppgave, med statusen du skal forvente i beskrivelsen eller testene.

Verktøyene du skal bruke:
- **Debugger**: et breakpoint i `CompanyController`, og kallstakken.
- **Actuator**: <http://localhost:8080/actuator/mappings>
- **Postman**: samlingen.

> **🔎 For validering:** `mvn verify` på `session2-start` er grønn med 3 tester *(målt, også på Boot 4.1.2 8. nov)*: listen er en array med headeren `X-Api-Version: 1`, og en gyldig POST gir en tom `201`. Kodefasiten er `git diff session2-start session2-solution`.

---

## Oppgave 2.1: Forespørselsdetektiv

**Mål:** uten å lese koden i controlleren eller filteret, finn ut:
1. Hvilken klasse og metode håndterer `POST /api/companies`?
2. Hvilken klasse gjør JSON-kroppen om til en `CompanyRequest`?
3. Hvilken klasse legger til responsheaderen `X-Api-Version`, og kjører den før eller etter `RequestContextFilter`?

**Hvorfor:** fra nå av kan du finne koden bak en hvilken som helst forespørsel i en hvilken som helst Spring Boot-applikasjon, også en du aldri har sett.

**Slik gjør du det:** åpne `/actuator/mappings` (let etter handleren for POST og etter `servletFilters`). Sett et breakpoint i `CompanyController.register`, send «Create company» fra Postman, og les kallstakken nedenfra. For spørsmål 2 søker du i debug-loggen eller listen over message converters.

**Ferdig når:** du kan navngi de tre klassene og rekkefølgen på de to filtrene.

> **🔎 For validering** *(målt på en ekte Tomcat, stakken med `jdb`; bønnenavnene og filterrekkefølgen også i MockMvc)*:
> 1. `CompanyController#register(CompanyRequest)` (fra `Mapping:`-linjene i demo-profilen).
> 2. `JacksonJsonHttpMessageConverter` (Jackson 3, pakken `org.springframework.http.converter.json`). Handler-adapteren har én instans av den, etter `ByteArray`-, `String`-, `Resource`-, `ResourceRegion`- og `AllEncompassingForm`-converterne *(målt på Boot 4.1.2)*. På Boot 3 het den `MappingJackson2HttpMessageConverter`, så eldre blogger og svar på Stack Overflow bruker det navnet. `RequestResponseBodyMethodProcessor` er argumentresolveren som kaller den.
> 3. `ApiVersionFilter`. I kallstakken står den **over** `RequestContextFilter`, noe som betyr at den kjører **etter** den (filterkjeden går `CharacterEncodingFilter`, `ServerHttpObservationFilter`, `FormContentFilter`, `RequestContextFilter`, `ApiVersionFilter`, deretter `DispatcherServlet`).
> *(målt: kallstakken på en ekte Tomcat-tråd, 54 rammer, med `ApiVersionFilter` mellom `WsFilter` og `RequestContextFilter`; `/actuator/mappings` lister 12 `dispatcherServlet`-oppføringer på demo-done. Not checked: den samme stakken vist i IntelliJ-panelet Frames.)*

---

## Oppgave 2.2: CRUD

**Mål:** API-et kan bare liste selskaper og opprette dem (og `POST` returnerer ingenting). Fullfør det:

| Forespørsel | Resultat |
|---|---|
| `GET /api/companies/{id}` | `200` med selskapet, eller `404` |
| `POST /api/companies` | `201` med en `Location`-header (`/api/companies/{id}`) og det opprettede selskapet i body |
| `PUT /api/companies/{id}` | `200` med det oppdaterte selskapet, eller `404` |
| `DELETE /api/companies/{id}` | `204` uten body, eller `404` |

**Hvorfor:** statuskoden og headerne er en del av API-et. En klient skal ikke måtte gjette om noe ble opprettet, endret eller ikke funnet.

**Slik gjør du det:**
1. `CompanyService` er ferdig: den har `find`, `update` og `delete` (les hva de returnerer). Du skriver bare controller-metoder i `CompanyController`. `TODO 2.2`-kommentarene viser hvor.
2. Bruk `ResponseEntity<Company>`. `ResponseEntity.created(uri).body(...)`, `ResponseEntity.noContent().build()` og `ResponseEntity.notFound().build()` er de du trenger.
3. Bygg `Location`-URI-en fra den gjeldende forespørselen: `ServletUriComponentsBuilder.fromCurrentRequest().path("/{id}")...`.
4. Test med mappen «CRUD (task 2.2)» i samlingen, fra øverst til nederst. «Create company» lagrer den nye id-en i en samlingsvariabel, slik at de neste forespørslene bruker den.

**Ferdig når:** hver forespørsel i mappen returnerer statusen som står i navnet eller beskrivelsen, og en andre DELETE på samme id gir `404`.

Merk: testen `CompanyStartTest.postGivesEmptyCreated` sjekker at `POST` gir en **tom** `201`, slik startprosjektet gjør. Den blir rød når du er ferdig med 2.2. Det er forventet: oppdater den til å sjekke body og `Location`, eller slett den.

> **🔎 For validering** *(målt, `CompanyCrudTest` på `session2-solution`)*: et selskap opprettes med `201`, `Location` som slutter på `/api/companies/{id}` og selskapet i body; `GET` gir `200`; `PUT` gir `200` og det nye navnet; `DELETE` gir `204`; etter den gir `GET` `404`. Ukjente id-er gir `404` for GET, PUT og DELETE. En gjentatt DELETE gir `404`.
> Postman-testskriptene passerer på `session2-solution` når de kjøres med `newman` (11 av 11 forespørsler, 11 av 11 påstander, målt på en ekte server). *8. nov:* den gamle samlingen sjekker bare statuskoder, så den kan ikke skille 2.3 fra 2.5, og den har ingen andre DELETE. Den forbedrede samlingen i `dag1-verifisering/okt2-mine-losninger.patch` sjekker også `Location`, body, en andre DELETE (`404`) og ProblemDetail; den gir 12 av 12 forespørsler og 16 av 16 påstander på min løsning *(målt med newman på Boot 4.1.2)*. Selve IntelliJ- eller Postman Collection Runner-vinduet er ikke sjekket.

---

## Oppgave 2.3: Valider et selskap

**Mål:** avvis et selskap med tomt navn eller et organisasjonsnummer som ikke er nøyaktig 9 siffer, med status `400`. Controlleren må ikke kalles.

**Hvorfor:** du så det i demoen: valideringen kjører mens argumentene løses. Nå setter du reglene selv.

**Slik gjør du det:** legg til `@NotBlank` og `@Pattern(regexp = "[0-9]{9}")` på feltene i `CompanyRequest` (`TODO 2.3`-kommentaren), og `@Valid` på `@RequestBody`-parameteren til både `POST` og `PUT`.

**Ferdig når:** `{"organizationNumber":"12","name":""}` gir `400`, og et breakpoint i controlleren blir **ikke** truffet. Bruk mappen «Validation» i samlingen.

> **🔎 For validering:** de samme annotasjonene er målt i `session2-demo-done` (status `400`, loggelinje fra `DefaultHandlerExceptionResolver`). På en ekte server er body i den 400-en Boots standard feil-JSON (`status`, `error`, `path`), **uten** feltfeil; det er ikke ProblemDetail med `errors` som du får etter oppgave 2.5. Bonusen i 2.5 er feltet `errors`.

---

## Oppgave 2.4: Din egen begrensning

**Mål:** erstatt `@Pattern` på organisasjonsnummeret med din egen annotasjon `@OrganizationNumber`, støttet av `OrgNumberValidator` fra sesjon 1.

**Hvorfor:** en constraint validator opprettes av Springs validator-fabrikk, og den kan få injisert beans gjennom konstruktøren. Du gjenbruker en bean du allerede har i stedet for å gjenta et regex.

**Slik gjør du det:**
1. Stubbene ligger i pakken `tasks`: `OrganizationNumber` (annotasjonen) og `OrganizationNumberConstraintValidator`. TODO-kommentarene forteller deg hva som mangler.
2. I annotasjonen peker du `validatedBy` til constraint-validatoren din.
3. I constraint-validatoren legger du til en konstruktørparameter `OrgNumberValidator` og kaller den fra `isValid`. **Ikke** annoter klassen med `@Component`.
4. Bruk `@OrganizationNumber` på organisasjonsnummeret i `CompanyRequest` (og fjern `@Pattern`).

**Ferdig når:** `{"organizationNumber":"12","name":"Acme"}` gir `400`, og `123456789` blir godtatt. Meldingen er den fra annotasjonen din: `must be exactly 9 digits`.

> **🔎 For validering** *(målt, `CompanyValidationTest` på `session2-solution`)*: `errors.organizationNumber` = `must be exactly 9 digits`, og et gyldig selskap gir `201`. Konstruktørinjeksjonen fungerer uten `@Component`.

---

## Oppgave 2.5: Lesbare feil

**Mål:** returner en `ProblemDetail` med status `400` og en property `errors` som knytter feltnavn til melding. Håndter også ugyldig JSON.

**Hvorfor:** standard-`400` sier ingenting om *hva* som var feil. En `@RestControllerAdvice` er en bean som `DispatcherServlet` spør når et kall kaster en feil.

**Slik gjør du det:**
1. `ApiExceptionHandler` (i `tasks`) er en tom `@RestControllerAdvice`. Legg til en metode med `@ExceptionHandler(MethodArgumentNotValidException.class)` som returnerer en `ProblemDetail`.
2. Les feilene fra `ex.getBindingResult().getFieldErrors()` (feltnavn og standardmelding). Legg dem til med `problem.setProperty("errors", ...)`.
3. Bonus: en andre handler for `HttpMessageNotReadableException` (prøv `{"name": ` som body) som returnerer `400` med detaljen `Malformed JSON request`.

**Ferdig når:** responsen er `application/problem+json` med `status` 400 og `errors.organizationNumber`, og `{"name": ` gir også 400 med din melding.

> **🔎 For validering** *(målt, `CompanyValidationTest`)*: `Content-Type: application/problem+json`, `status` 400, `errors.name` = `must not be blank`, `errors.organizationNumber` = `must be exactly 9 digits`. `{}` gir feil for begge feltene. Ugyldig JSON gir `detail: "Malformed JSON request"`. Uten bonus-handleren gir ugyldig JSON også 400, med Boots standard feil-JSON (`timestamp`, `status`, `error`, `path`) *(målt på Boot 4.1.2)*.

---

## Oppgave 2.6: Tilleggsoppgave, et filter som måler tid

**Mål:** implementer `RequestTimingFilter` (stub i `tasks`) som en `OncePerRequestFilter`-bean som logger metode, sti, status og varighet i ms for hver forespørsel. Svar deretter: dukker loggelinjen opp for en forespørsel som feiler validering? Og for en som får `404`?

**Hvorfor:** et filter er en bean i kjeden du så i kallstakken. Det omslutter `DispatcherServlet`, så det ser alt.

**Slik gjør du det:** gjør klassen til en `@Component`, ta tiden med `System.nanoTime()` før `chain.doFilter(...)`, og logg etter den (i en `finally`-blokk).

**Ferdig når:** hver forespørsel i Postman-samlingen gir én loggelinje.

> **🔎 For validering** *(målt i testloggen til `RequestTimingFilterTest`)*: ja, begge logges, for eksempel `POST /api/companies -> 201 in 42 ms` og `POST /api/companies -> 400 in 8 ms` (tidene varierer). `404` logges også (`GET /api/companies/99 -> 404 in 0 ms`).

---

## 🔎 Oppsummering: hva som ikke er sjekket

- Statuser, headere og bodyer er målt på en ekte server med `curl` og `newman` (ikke klikket i Postman-appen). Selve Postman-appen er ikke sjekket.
- IntelliJ-grensesnittet: Debug-panelet og Frames-visningen. Selve stakken er målt med `jdb`.
- Windows.
