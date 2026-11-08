# Økt 2: forelesers manus (steg, hva og hvorfor)

Økt 2 går fra 11:10 til 13:10 (30 min teori og demoer, deretter oppgaver og 15 min felles gjennomgang). Det er **ingen innsjekking**: de første 3 minuttene er en repetisjon av økt 1 og en rask sjekk av oppsettet. Dagsplanen finnes i `materials/day1-schedule.md`.

Dette er stegene du utfører foran salen, i rekkefølge. For hvert steg får du **hva** du gjør, **hva skjer**, **forventet** output og **hvorfor** vi gjør det (budskapet til deltakerne).

Verdier merket *(measured)* er observert ved å kjøre prosjektet: `session-2` bygget med Temurin 25.0.5, Spring Boot 4.1.2, Maven. Svar, headere og kropper ble målt mot en **ekte Tomcat** (`java -jar`, `curl`, `newman`), og kallstakken i en **ekte JDWP-feilsøkingssesjon** (`jdb`). Verdier fra `MockMvc` er merket som det. Punkter merket *(not checked)* har jeg ikke klart å kjøre: selve IntelliJ-grensesnittet (se listen til slutt).

---

## Den røde tråden

Økt 1 viste hvordan beans opprettes. Økt 2 viser hvordan en forespørsel **finner** dem: socket, Tomcat, filtre, `DispatcherServlet`, handler mapping, argumentoppløsning (Jackson, `@Valid`), controller-bean, returverdi, respons.

Aha-øyeblikket: *«En forespørsel er en kallstakk gjennom beans, og jeg kan lese den.»* Dette er den samme ideen som i økt 1 («Spring er ikke magi, jeg kan åpne panseret selv»).

Fire demoer bærer økten:

1. D1: et breakpoint i controlleren, og kallstakken leses fra bunnen.
2. D2: oppslagstabellen (handler mapping) er en bean du kan spørre, og `/actuator/mappings` skriver den ut.
3. D3: validering ligger mellom forespørselen og controlleren: breakpointet blir aldri truffet.
4. D4: `@RestControllerAdvice` er en bean som `DispatcherServlet` spør når et kall kaster en feil.

---

## Før du starter (dagen før, og 10 min før)

1. Åpne prosjektet `session-2` i IntelliJ. Maven-synk, SDK 25.
2. Opprett en branch slik at du kan forkaste alle endringer etterpå:

   ```
   git checkout -b demo session2-demo-done
   ```

   **Hvorfor:** du fjerner og legger til `@Valid` live. `git checkout -- .` tar deg tilbake. Taggen `session2-demo-done` er nødplanen din.
3. Opprett to kjørekonfigurasjoner av `CompanyApplication`: **demo** (Active profiles: `demo`) og **demo+advice** (Active profiles: `demo,advice`). Profilen `demo` slår på `HandlerMappingPrinter` og DEBUG-logging for forespørselsstien. Profilen `advice` slår på `DemoExceptionHandler`.
4. Importer `postman/session2.postman_collection.json` og `postman/local.postman_environment.json` i Postman, velg miljøet "Brights local". Sjekk at "Create company" er forespørselen du bruker i D1.
5. Kjør alle fire demoene én gang. Ta skjermbilder som plan B: kallstakken fra D1 (med skjulte rammer utvidet) og de to responsene fra D3/D4.
6. Port 8080 er ledig. Stor skriftstørrelse i IDE-en. Tøm konsollen før hver demo.
7. `mvn verify` er grønn på alle tre taggene *(measured)*, se «Målte resultater» til slutt.

---

## Åpning (lysbilde 1-4, 4 min)

- Lysbilde 1 (tittel) og 2 (agenda): 1 min sammen. Agendaen viser hele dagen.
- Lysbilde 3 (mål): repetisjon av økt 1 i to setninger: *«Sist: en bean opprettes fra en klasse av containeren. Beans kobles sammen gjennom konstruktøren.»* Deretter de tre målene for denne økten.
- Lysbilde 4 (sjekk av oppsett): `git checkout -b my-answers session2-start`, start appen, åpne `/actuator/health`, importer Postman-samlingen og kjør "Health". Håndsopprekning: hvem står fast? De som står fast fortsetter å jobbe mens du starter lysbilde 5; hjelp dem i den første oppgaveblokken.

---

## D1. Følg en forespørsel i debuggeren (6 min)

**Mål:** veien fra socketen til controlleren vår er vanlige Java-kall som du kan lese.

Filer: `web/CompanyController.java` (breakpoint på linjen `service.register(...)`, merket med en kommentar), `web/ApiVersionFilter.java`.

### Steg 1: sett breakpointet og send forespørselen (1 min)

**Hva:** kjør **demo** i Debug. Send "Create company" i Postman (`POST /api/companies`, body `{"organizationNumber":"123456789","name":"Acme Corp"}`). IntelliJ stopper i controlleren.

**Hva skjer:** Postman venter. Fanen *Frames* viser kallstakken. *(breakpointet og stakken ble målt med `jdb` koblet til en ekte Tomcat-tråd `http-nio-8080-exec-N`; selve IntelliJ-panelet Frames er not checked)*

### Steg 2: les stakken nedenfra (3 min)

**Hva:** i *Frames* klikker du på *Show/Hide library frames* slik at de skjulte rammene vises. Les fra bunnen og opp.

**Forventet** *(measured with `jdb` on a real Tomcat thread, `http-nio-8080-exec-5`, 54 rammer; innerst først)*:

```
CompanyController.register                          (CompanyController.java:36)
InvocableHandlerMethod.doInvoke
InvocableHandlerMethod.invokeForRequest
ServletInvocableHandlerMethod.invokeAndHandle
RequestMappingHandlerAdapter.invokeHandlerMethod
RequestMappingHandlerAdapter.handleInternal
AbstractHandlerMethodAdapter.handle
DispatcherServlet.doDispatch
DispatcherServlet.doService
FrameworkServlet.processRequest
FrameworkServlet.doPost
HttpServlet.service
FrameworkServlet.service
HttpServlet.service
ApplicationFilterChain.internalDoFilter / doFilter
WsFilter.doFilter                                   <- Tomcat WebSocket filter
ApplicationFilterChain.internalDoFilter / doFilter
ApiVersionFilter.doFilterInternal                   <- our own filter
OncePerRequestFilter.doFilter
RequestContextFilter.doFilterInternal
FormContentFilter.doFilterInternal
ServerHttpObservationFilter.doFilterInternal
CharacterEncodingFilter.doFilterInternal
StandardWrapperValve.invoke
StandardContextValve.invoke
AuthenticatorBase.invoke
StandardHostValve.invoke
ErrorReportValve.invoke
StandardEngineValve.invoke
CoyoteAdapter.service
... Http11Processor, the socket thread pool, Thread.run (54 frames in total)
```

(Øverst = innerst. Rammene uten en klasse fra oss er like i hver forespørsel; de du skal vise er `ApiVersionFilter`, `DispatcherServlet.doDispatch` og `RequestMappingHandlerAdapter`.) Pek på tre steder: **(1)** filterkjeden, hvert filter kaller det neste (`ApplicationFilterChain.doFilter`), **(2)** `DispatcherServlet.doDispatch`, **(3)** `RequestMappingHandlerAdapter`, som løser opp argumentene og kaller metoden vår.
**Hvorfor:** forespørselsstien er ikke magi. Den er en kjede av beans og vanlige metodekall, den samme ideen som i økt 1.

### Steg 3: se på argumentet (1 min)

**Hva:** i *Variables* åpner du `request`. Den er allerede en `CompanyRequest`.

**Hva skjer:** loggen (profilen `demo`) har en DEBUG-linje fra `RequestResponseBodyMethodProcessor` *(measured)*:

```
Read "application/json;charset=UTF-8" to [CompanyRequest[organizationNumber=123456789, name=Acme Corp]]
```

og før den: `Mapped to no.course.company.web.CompanyController#register(CompanyRequest)` fra `RequestMappingHandlerMapping` *(measured)*.

**Hvorfor:** Jackson (en `HttpMessageConverter`) gjorde konverteringen fra JSON til objekt *før* koden vår kjørte. Controlleren ser aldri JSON-tekst.

### Steg 4: fortsett (1 min)

**Hva:** trykk Resume. Se på responsen i Postman.

**Forventet** *(measured with MockMvc)*: status `201`, **tom body** (metoden returnerer `void`), header `X-Api-Version: 1`.

**Hvorfor / spør salen:** *"Hvem la til `X-Api-Version`?"* Vi skrev den ikke i controlleren. Dette er oppgave 2.1. Svar: `ApiVersionFilter`, en av rammene i stakken.

**Plan B:** vis reservelysbilde 12 (stakken over).

---

## D2. Handler mappings: oppslagstabellen er en bean (3 min)

**Mål:** `DispatcherServlet` finner controlleren gjennom en tabell, og tabellen er en bean du kan spørre.

### Steg 1: oppstartsloggen (1 min)

**Hva:** kjør **demo** (ikke Debug), tøm konsollen, start på nytt. Finn linjene `Mapping:`. Åpne `demo/HandlerMappingPrinter.java` (omtrent 10 linjer kode: den injiserer `RequestMappingHandlerMapping` og skriver ut `getHandlerMethods()`).

**Forventet** *(measured, rekkefølgen er ikke garantert)*:

```
Mapping: {POST [/api/companies]} -> no.course.company.web.CompanyController#register(CompanyRequest)
Mapping: { [/error]} -> org.springframework.boot.webmvc.autoconfigure.error.BasicErrorController#error(HttpServletRequest)
Mapping: { [/error], produces [text/html]} -> org.springframework.boot.webmvc.autoconfigure.error.BasicErrorController#errorHtml(HttpServletRequest, HttpServletResponse)
Mapping: {GET [/api/companies]} -> no.course.company.web.CompanyController#all()
```

**Hvorfor:** en tabell fra "HTTP-metode og sti" til "controller-metode". `/error` er Spring Boots egen controller.

### Steg 2: `/actuator/mappings` (2 min)

**Hva:** Postman-forespørselen "Mappings (task 2.1)" eller nettleseren: <http://localhost:8080/actuator/mappings>. Finn `CompanyController#register` og `servletFilters`.

**Hva skjer** *(measured on a real server, `session2-demo-done`)*: `servletFilters` lister `requestContextFilter`, `webMvcObservationFilter`, `Tomcat WebSocket (JSR356) Filter`, `characterEncodingFilter`, `apiVersionFilter` og `formContentFilter`. På `session2-solution` har listen også `requestTimingFilter`. `dispatcherServlets.dispatcherServlet` har 12 oppføringer på demo-done og 15 på løsningen: controller-metodene (`register`, `all`, `get`, `delete`), feilcontrolleren, og på løsningen actuator-endepunktene. (Tidligere skrev jeg at listen var tom; det var MockMvc.)

**Hvorfor:** den samme informasjonen som debuggeren, uten å stoppe appen. Oppgave 2.1 bruker den.

**Plan B:** linjene `Mapping:` på lysbilde 6 er nok.

---

## D3. Validering ligger mellom forespørselen og controlleren (5 min)

**Mål:** vise at validering skjer mens argumentene løses opp, slik at controlleren aldri kalles.

### Steg 1: uten `@Valid` (1 min)

**Hva:** kjør **demo** i Debug. I `CompanyController` sletter du `@Valid` fra `register`. Start på nytt. POST `{"organizationNumber":"12","name":""}`.

**Hva skjer:** breakpointet treffes, søppeldataene registreres, `201`.

### Steg 2: med `@Valid` (2 min)

**Hva:** skriv `@Valid` tilbake. Start på nytt. POST samme body.

**Hva skjer:** breakpointet blir **ikke** truffet. Status `400`. Loggen har *(measured)*:

```
WARN ... .w.s.m.s.DefaultHandlerExceptionResolver : Resolved [org.springframework.web.bind.MethodArgumentNotValidException: Validation failed for argument [0] in public void no.course.company.web.CompanyController.register(no.course.company.web.CompanyRequest) with 2 errors: [...]
```

Body-en på en ekte server (`session2-demo-done`, curl) er Spring Boots standard feil-JSON: `{"timestamp":"...","status":400,"error":"Bad Request","path":"/api/companies"}`. Den har **ingen** feltfeil. (Under MockMvc er body tom.)

### Steg 3: hvor ligger reglene? (1 min)

**Hva:** åpne `CompanyRequest`: `@Pattern(regexp = "[0-9]{9}", ...)` på organisasjonsnummeret og `@NotBlank` på navnet.

**Hvorfor:** validering = annotasjoner på dataene, `@Valid` = "kjør dem nå". Spør: *"Hvilken bean gjorde unntaket om til en 400?"* Svar: `DefaultHandlerExceptionResolver`, den står i logglinjen.

**Plan B:** `git checkout session2-demo-done` og kjør bare steg 2.

---

## D4. Feil er også beans: `@RestControllerAdvice` (3 min)

**Mål:** standard-400-svaret har ingen feltfeil. En advice-bean gir klienten noe nyttig.

**Hva:** stopp. Kjør **demo+advice** (profilene `demo,advice`). POST samme body. Åpne `demo/DemoExceptionHandler.java` (omtrent 15 linjer).

**Forventet** *(measured on a real server, `session2-solution`; samme body i MockMvc)*: status `400`, `Content-Type: application/problem+json`, body:

```json
{"detail":"Validation failed","instance":"/api/companies","status":400,"title":"Bad Request","errors":{"name":"must not be blank","organizationNumber":"must be exactly 9 digits"}}
```

Logglinjen endrer seg *(measured)*: `ExceptionHandlerExceptionResolver` (DEBUG) løser unntaket i stedet for `DefaultHandlerExceptionResolver` (WARN).

**Hvorfor:** `DispatcherServlet` spør en liste med unntaksløsere når et kall kaster en feil. `@RestControllerAdvice` er en bean i den listen. Dette er oppgave 2.5.

**Plan B:** reservelysbilde 13 (før og etter).

---

## Tilbakestilling

```
git checkout -- .
git clean -fd
git checkout main
```

(Forkaster live-endringer i `demo`-branchen. Taggene er urørt.) For oppgavene bruker deltakerne `session2-start`. Til felles gjennomgang bruker du `session2-solution`.

---

## Felles gjennomgang (15 min, `session2-solution`, 12:55)

```
git checkout session2-solution
```

1. **(5 min) Oppgave 2.2, CRUD.** Åpne `CompanyController`. Gå gjennom tabellen: `POST` gir `201` med `Location`, `GET /{id}` gir `200` eller `404`, `PUT` gir `200` eller `404`, `DELETE` gir `204` eller `404`. Spør: *"Hva returnerer en andre DELETE på samme id?"* Svar: `404` *(measured)*. Kjør Postman-samlingen med Collection Runner hvis tiden strekker til. *(Measured with `newman`, kommandolinjemotoren til Collection Runner: 11 av 11 forespørsler og 11 av 11 assertions består på `session2-solution`. På `session2-demo-done` feiler de tre CRUD-forespørslene `GET`/`PUT`/`DELETE` med `404`, fordi CRUD er oppgave 2.2. På `session2-start` feiler 5 av 11 assertions.)*
2. **(4 min) Oppgave 2.4, egen constraint.** Åpne `OrganizationNumberConstraintValidator`. Spør: *"Hvorfor fungerer den uten `@Component`?"* Svar: Springs validator-fabrikk oppretter constraint-validatoren og autowirer konstruktøren, slik at `OrgNumberValidator` (økt 1) blir injisert. Validatoren er en bean fra økt 1, gjenbrukt på et nytt sted.
3. **(3 min) Bonusoppgave 2.6, timing-filteret.** Loggen viser begge forespørslene *(measured)*: `POST /api/companies -> 201 in 42 ms` og `POST /api/companies -> 400 in 8 ms`. Filteret omslutter `DispatcherServlet`, så det ser også mislykket validering. (Tider varierer.)
4. **(3 min) Spørsmål og bro til økt 3.** *"Grensen for registrering og varslingskanalen er fortsatt fastsatt i koden. Neste: hvor kommer verdiene fra, og hvordan kjører vi den samme appen i tre miljøer?"*

---

## Målte resultater

`mvn verify` (Spring Boot 4.1.2, Temurin 25.0.5) er grønn på alle tre taggene *(measured)*:

| Tag | Tester |
|---|---|
| `session2-start` | 3 (konteksten laster, GET returnerer array med `X-Api-Version`, POST gir tom 201) |
| `session2-demo-done` | 5 (de 3 over, mappinger skrevet ut, advice gir `errors`) |
| `session2-solution` | 13 (CRUD-livssyklus, `Location`, 404-er, validering, egen constraint, ugyldig JSON, logg fra timing-filteret, advice) |

Andre svar, målt på en ekte server (`java -jar`, `curl -i`, `session2-solution` hvis ikke annet er angitt):

| Forespørsel | Status | Merknader |
|---|---|---|
| `POST` med gyldig body | 201 | `Location: http://localhost:8080/api/companies/1`, body med selskapet. På `session2-demo-done` gir samme forespørsel 201 med **ingen** `Location` og `Content-Length: 0` |
| `POST` uten `Content-Type` (`curl -d` sender skjemadata) eller med `text/plain` | 415 | `Accept: application/json, application/*+json` og Boots standard feil-JSON (`status` 415, `error` "Unsupported Media Type") |
| `PUT /api/companies` (uten id) | 405 | `Allow: GET, POST`, Boots standard feil-JSON |
| `GET /api/companies/abc` | 400 | typefeil, Boots standard feil-JSON, ingen `errors` |
| `GET /api/nope` | 404 | Boots standard feil-JSON med `path` |
| `POST` med ugyldig JSON | 400 | `detail: "Malformed JSON request"` (bonus i oppgave 2.5), ProblemDetail |
| `POST {}` | 400 | ProblemDetail med `errors` for både `name` og `organizationNumber` |
| `POST {}` på `session2-demo-done` | 400 | Boots standard feil-JSON, **ingen** feltfeil |

MockMvc-testene i prosjektet gir de samme statusene; kroppene er forskjellige (MockMvc: tomme kropper for 415, 405, 400).

---

## Det jeg ikke har sett selv (sjekk under øving)

- IntelliJ-grensesnittet: Debug, Frames-panelet, skjulte library-rammer, kjørekonfigurasjoner med profiler. Stakken ble målt med `jdb` på samme JVM; selve panelet er not checked.
- Postman: forespørslene ble sendt med `curl` og `newman`, ikke klikket i Postman-appen. Selve Postman-appen er not checked.
- En kjøring på en annen maskin enn min, og på Windows.
- Lysbildene: `materials/build_session2_slides.py` (14 lysbilder) er sjekket med et estimat for tekstplass og en Quick Look-rendring av hvert lysbilde. Not checked: PowerPoint selv (PDF-eksporten ble ikke ferdig i denne sesjonen).

---

## Lysbilder og demoer: når du bytter

Presentasjonen er `session2-slides.pptx` (14 lysbilder, bygget av `materials/build_session2_slides.py`). Talernotatene på hvert lysbilde sier hva du skal si og når du skal bytte.

| Tid | Modus | Lysbilder / demo | Min |
|---|---|---|---|
| 11:10 | Lysbilder | 1 Tittel, 2 Agenda, 3 Mål (repetisjon), 4 Sjekk av oppsett | 4 |
| 11:14 | Lysbilder | 5 Forespørselsstien | 2 |
| 11:16 | **Demo 1** (IntelliJ, Postman) | Følg en forespørsel i debuggeren | 6 |
| 11:22 | Lysbilder | 6 Hvem håndterer forespørselen? | 1 |
| 11:23 | **Demo 2** (IntelliJ, nettleser) | Handler mappings, `/actuator/mappings` | 3 |
| 11:26 | Lysbilder | 7 CRUD: verb, statuskode, header | 1 |
| 11:27 | Lysbilder | 8 Validering ligger før controlleren | 2 |
| 11:29 | **Demo 3** (IntelliJ, Postman) | `@Valid` på og av | 5 |
| 11:34 | Lysbilder | 9 Feil er også beans | 1 |
| 11:35 | **Demo 4** (IntelliJ, Postman) | `@RestControllerAdvice`, ProblemDetail | 3 |
| 11:38 | Lysbilder | 10 Feil du vil møte | 1 |
| 11:39 | Lysbilder | 11 Din tur | 1 |
| 11:40 | Oppgaver | deltakerne jobber (lysbilde 11 står oppe) | 75 |
| 12:55 | Gjennomgang | felles gjennomgang på `session2-solution` | 15 |

Reservelysbilder: 12 (kallstakk, hvis debuggeren feiler i demo 1), 13 (før og etter advice, hvis demo 3 eller 4 feiler), 14 (filterrekkefølge, hvis `/actuator/mappings` feiler i demo 2).
