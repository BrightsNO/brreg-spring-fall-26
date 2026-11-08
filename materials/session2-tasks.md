# Økt 2: oppgaver

Du skal bruke forespørselsløpet selv: finn veien gjennom det, bygg de manglende endepunktene, og få API-et til å si nei på en nyttig måte. Hver oppgave har **Mål** (hva), **Hvorfor** (hva du lærer), **Slik gjør du det** og **Ferdig når**.

Oppgave 2.6 er for dem som blir ferdige tidlig.

---

## Før du begynner

1. Branchen har øktnummer i navnet, slik at den ikke kolliderer med branchene fra de andre øktene:

   ```
   git checkout -b my-answers-2 session2-start
   ```

   **Hvorfor en egen branch:** du kommer til å endre kode, og vil kunne sammenligne med fasiten uten å miste noe.
2. Åpne mappen `session-2/` (ikke repo-roten) i IntelliJ og start `CompanyApplication`. Åpne <http://localhost:8080/actuator/health>. Du skal se `{"status":"UP"}`.
3. Importer filene i `postman/` til Postman (samlingen «Spring Boot course: session 2» og miljøet «Brights local») og velg miljøet. Samlingen har forespørslene du trenger for hver oppgave, med statusen du skal forvente i beskrivelsen eller testene.

Verktøyene du skal bruke:
- **Debugger**: et breakpoint i `CompanyController`, og kallstakken.
- **Actuator**: <http://localhost:8080/actuator/mappings>
- **Postman**: samlingen.

---

## Oppgave 2.1: Forespørselsdetektiv

**Mål:** uten å lese koden i controlleren eller filteret, finn ut:
1. Hvilken klasse og metode håndterer `POST /api/companies`?
2. Hvilken klasse gjør JSON-kroppen om til en `CompanyRequest`?
3. Hvilken klasse legger til responsheaderen `X-Api-Version`, og kjører den før eller etter `RequestContextFilter`?

**Hvorfor:** fra nå av kan du finne koden bak en hvilken som helst forespørsel i en hvilken som helst Spring Boot-applikasjon, også en du aldri har sett.

**Slik gjør du det:** åpne `/actuator/mappings` (let etter handleren for POST og etter `servletFilters`). Sett et breakpoint i `CompanyController.register`, send «Create company» fra Postman, og les kallstakken nedenfra. For spørsmål 2 søker du i debug-loggen eller listen over message converters.

**Ferdig når:** du kan navngi de tre klassene og rekkefølgen på de to filtrene.

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

---

## Oppgave 2.3: Valider et selskap

**Mål:** avvis et selskap med tomt navn eller et organisasjonsnummer som ikke er nøyaktig 9 siffer, med status `400`. Controlleren må ikke kalles.

**Hvorfor:** du så det i demoen: valideringen kjører mens argumentene løses. Nå setter du reglene selv.

**Slik gjør du det:** legg til `@NotBlank` og `@Pattern(regexp = "[0-9]{9}")` på feltene i `CompanyRequest` (`TODO 2.3`-kommentaren), og `@Valid` på `@RequestBody`-parameteren til både `POST` og `PUT`.

**Ferdig når:** `{"organizationNumber":"12","name":""}` gir `400`, og et breakpoint i controlleren blir **ikke** truffet. Bruk mappen «Validation» i samlingen.

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

---

## Oppgave 2.5: Lesbare feil

**Mål:** returner en `ProblemDetail` med status `400` og en property `errors` som knytter feltnavn til melding. Håndter også ugyldig JSON.

**Hvorfor:** standard-`400` sier ingenting om *hva* som var feil. En `@RestControllerAdvice` er en bean som `DispatcherServlet` spør når et kall kaster en feil.

**Slik gjør du det:**
1. `ApiExceptionHandler` (i `tasks`) er en tom `@RestControllerAdvice`. Legg til en metode med `@ExceptionHandler(MethodArgumentNotValidException.class)` som returnerer en `ProblemDetail`.
2. Les feilene fra `ex.getBindingResult().getFieldErrors()` (feltnavn og standardmelding). Legg dem til med `problem.setProperty("errors", ...)`.
3. Bonus: en andre handler for `HttpMessageNotReadableException` (prøv `{"name": ` som body) som returnerer `400` med detaljen `Malformed JSON request`.

**Ferdig når:** responsen er `application/problem+json` med `status` 400 og `errors.organizationNumber`, og `{"name": ` gir også 400 med din melding.

---

## Oppgave 2.6: Tilleggsoppgave, et filter som måler tid

**Mål:** implementer `RequestTimingFilter` (stub i `tasks`) som en `OncePerRequestFilter`-bean som logger metode, sti, status og varighet i ms for hver forespørsel. Svar deretter: dukker loggelinjen opp for en forespørsel som feiler validering? Og for en som får `404`?

**Hvorfor:** et filter er en bean i kjeden du så i kallstakken. Det omslutter `DispatcherServlet`, så det ser alt.

**Slik gjør du det:** gjør klassen til en `@Component`, ta tiden med `System.nanoTime()` før `chain.doFilter(...)`, og logg etter den (i en `finally`-blokk).

**Ferdig når:** hver forespørsel i Postman-samlingen gir én loggelinje.
