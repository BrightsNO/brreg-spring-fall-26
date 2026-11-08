# Økt 4: oppgaver

Du skal selv bruke verktøyene fra demoene, og bygge sikkerhetskonfigurasjonen til API-et steg for steg. Hver oppgave har **Mål** (hva), **Hvorfor** (hva du lærer), **Slik gjør du det** og **Ferdig når**.

Dokumentet har to typer innhold:
- Vanlig tekst er oppgaveteksten deltakerne får.
- Bokser merket **🔎 For validering** er for deg som validerer oppsettet. De fjernes før oppgavene deles ut.

---

## Før du starter

1. Ha JDK 25, IntelliJ, Maven og Postman.
2. Klon repoet og sjekk ut `session4-start`:

   ```
   git checkout -b my-answers session4-start
   ```

   **Hvorfor en branch:** du skal endre kode, og vil kunne sammenligne med fasiten uten å miste noe.
3. Åpne prosjektet i IntelliJ. Maven-vinduet skal vise `company-session4` (høyreklikk `pom.xml` → *Maven → Sync Project* ved behov).
4. Start `CompanyApplication` (grønn pil). Se etter `Using generated security password: ...` i konsollen.
5. Importer `postman/session4.postman_collection.json` i Postman (*Import*). Collectionen har variablene `baseUrl`, `user`, `userPassword`, `admin` og `adminPassword`.
6. Send request **01 Public info**. Du skal få `401`. Ja, selv det «offentlige» endpointet er låst. Det er der vi starter.

Verktøyene du skal bruke:
- **Postman**: collectionen, og *Authorization*-fanen (type *Basic Auth*).
- **TRACE-logg**: start med profilen `trace` (*Run → Edit Configurations → Active profiles*: `trace`). Gir én linje for hvert filter requesten går gjennom.
- **Nettleserens utviklerverktøy** (F12), fanene *Console* og *Network*: til oppgave 4.5.

> **🔎 For validering:** `session4-start` skal starte, skrive ut et generert passord, og gi `401` på `GET /public/info` og `GET /api/companies`. `GET /actuator/health` gir `200` *(målt)*. Spring Boot slipper health-endpointet gjennom selv i standardoppsettet. Kodefasiten er `git diff session4-start session4-solution`.

---

## Oppgave 4.1: Filterdetektiv

**Mål:**
1. Logg inn med det genererte passordet: Postman-request **03**, *Authorization → Basic Auth*, bruker `user` og passordet fra konsollen. Du skal få `200` i stedet for `401`.
2. Skriv `FilterChainPrinter` (en `CommandLineRunner`) som skriver ut hvert filter i filterkjeden til Spring Security, i rekkefølge.
3. Svar:
   - Hvor mange filtre er det i kjeden?
   - Hvilket filter leser `Authorization`-headeren?
   - Hvilket filter sjekker CSRF-tokens? Kommer det før eller etter filteret som leser headeren?
   - Hvilket filter er sist?
4. Start med profilen `trace`, send request **03** uten innlogging, og finn linjen `Securing GET /api/companies`. Følg `Invoking ... (n/17)`-linjene. Hvilket filter ender opp med å bestemme at svaret blir `401`?

**Hvorfor:** Spring Security er en liste med filtre foran controlleren din. Når du kan skrive ut listen og lese loggen, kan du selv feilsøke «hvorfor får jeg 401/403?».

**Slik gjør du det:**
- `FilterChainPrinter` finnes som en tom stub i `tasks/`, med en TODO-kommentar.
- Gjør den til en `@Component` som implementerer `CommandLineRunner`. Hent `FilterChainProxy` via konstruktøren. Den har `getFilterChains()`, og hver `SecurityFilterChain` har `getFilters()`. Skriv ut `filter.getClass().getSimpleName()`.
- For loggen: søk i konsollen (Ctrl+F) etter `Securing`, `Invoking` og `entry point`.

**Ferdig når:** du har filterlisten og har svart på de fire spørsmålene.

> **🔎 For validering** *(målt, Spring Boot 4.1.2, Spring Security 7.1.2)*:
>
> Standardkjeden har **17** filtre:
> ```
> 1 DisableEncodeUrlFilter                 10 DefaultLoginPageGeneratingFilter
> 2 WebAsyncManagerIntegrationFilter       11 DefaultLogoutPageGeneratingFilter
> 3 SecurityContextHolderFilter            12 BasicAuthenticationFilter
> 4 HeaderWriterFilter                     13 RequestCacheAwareFilter
> 5 CorsFilter                             14 SecurityContextHolderAwareRequestFilter
> 6 CsrfFilter                             15 AnonymousAuthenticationFilter
> 7 LogoutFilter                           16 ExceptionTranslationFilter
> 8 UsernamePasswordAuthenticationFilter   17 AuthorizationFilter
> 9 DefaultResourcesFilter
> ```
> - Leser `Authorization`: `BasicAuthenticationFilter` (12). CSRF: `CsrfFilter` (6), **før** det. Sist: `AuthorizationFilter`.
> - Hvem sier 401: `AuthorizationFilter` avviser den anonyme brukeren (`AuthorizationDeniedException`), og `ExceptionTranslationFilter` fanger den og sender requesten til authentication entry point. Logglinjen er `Sending AnonymousAuthenticationToken ... to authentication entry point`. Godt svar: «det siste filteret sier nei, det nest siste gjør det om til 401».
> - Det finnes bare én kjede, definert av Spring Boot (`managementSecurityFilterChain` i TRACE-loggen).
> - Postman får `401` med `WWW-Authenticate: Basic realm="Realm", charset="UTF-8"`. En nettleser (header `Accept: text/html`) får `302` til den genererte innloggingssiden *(målt med curl)*.
>
> Fasit: `tasks/FilterChainPrinter.java` på taggen `session4-solution`. Merk at fasiten skriver ut 14 filtre, fordi kjeden fra oppgave 4.2 ikke har form login og ikke noe CSRF-filter.

---

## Oppgave 4.2: Dine egne regler

**Mål:** erstatt standardoppsettet med din egen `SecurityFilterChain` og dine egne brukere.

| Request | Hvem har lov |
|---|---|
| `/public/**`, `/actuator/health`, `/error` | alle |
| `GET /api/**` | rollen `USER` eller `ADMIN` |
| `POST /api/**`, `DELETE /api/**` | rollen `ADMIN` |
| alt annet | ingen |

Brukere (foreløpig med passord i klartekst, det fikser vi i 4.3): `user` / `password` med rollen `USER`, og `admin` / `admin` med rollen `ADMIN`.

Sjekk med Postman-requestene **01 - 08 og 11**. Requestene **09 og 12** vil gi `403` også for admin. Ikke fiks det ennå. Det er oppgave 4.4. (Request **10** gir `404` så lenge 09 ikke har opprettet selskapet.)

**Hvorfor:** dette er konfigurasjonen du kommer til å skrive og lese i alle Spring Security-prosjekter. Rekkefølgen på reglene og hva som avvises som standard, er der de fleste reelle feilene gjøres.

**Slik gjør du det:**
- `SecurityConfig` finnes som en tom stub i `tasks/`. Gjør den til en `@Configuration`.
- En `@Bean SecurityFilterChain filterChain(HttpSecurity http)` som kaller `http.authorizeHttpRequests(...)` og `.httpBasic(Customizer.withDefaults())`. Reglene sjekkes **i rekkefølge, første treff vinner**.
- En `@Bean UserDetailsService` som returnerer `new InMemoryUserDetailsManager(...)`. Bygg brukere med `User.withUsername("user").password("{noop}password").roles("USER").build()`. `{noop}` betyr «klartekst, ingen encoder».
- Bruk `hasRole("ADMIN")` og `hasAnyRole(...)`. Spring legger til prefikset `ROLE_` for deg.

**Svar deretter, ved å prøve det:**
1. Kall **05 Who am I** som `user`. Hvilken streng står i `authorities`? Hva er forskjellen på `hasRole("ADMIN")` og `hasAuthority("ROLE_ADMIN")`?
2. Kall request **08** (POST som `user`) og request **03** (ingen innlogging). Den ene gir `403` og den andre `401`. Hvorfor?
3. Fjern `"/error"` fra `permitAll()`-listen, start på nytt, og kall `GET /api/companies/000` som `user` (et selskap som ikke finnes). Hvilken status får du nå, og hva forventet du? Sett `/error` tilbake.
4. Flytt `anyRequest().denyAll()`-linjen til toppen. Hva skjer?

**Ferdig når:** requestene 01-08 og 11 i collectionen gir forventet status, og du kan svare på de fire spørsmålene.

> **🔎 For validering** *(målt i `session4-solution` og i `d3`)*:
> - 01 → 200, 02 → 200, 03 → 401, 04 → 200, 05/06 → 200, 07 (feil passord) → 401, 08 → 403, 11 → 403. 09 og 12 → **403** (CSRF) frem til 4.4, og 10 → **404** så lenge 09 ikke har opprettet selskapet (etter 4.4: 09 → 201, 10 → 200, 12 → 204).
> - Sp1: `["FACTOR_PASSWORD","ROLE_USER"]`. `FACTOR_PASSWORD` er ny i Spring Security 7: den sier hvordan brukeren logget inn og er ikke en rolle (se økt 5, der den må holdes utenfor `roles`-claimet). `hasRole("ADMIN")` er det samme som `hasAuthority("ROLE_ADMIN")`. Admin får `["FACTOR_PASSWORD","ROLE_ADMIN"]`.
> - Sp2: 401 = ikke autentisert (vi vet ikke hvem du er). 403 = autentisert, men rollen har ikke lov. Ukjent sti som anonym gir 401, som `admin` gir den 403 (`denyAll`).
> - Sp3: uten `/error` i `permitAll()` blir 404 til **403**. En feil i controlleren går via `/error`, som også er sikret, og `denyAll()` treffer den. Med en `authenticated()`-regel i stedet for `denyAll()` avhenger resultatet av requesten, og det er like forvirrende. Dette er en vanlig overraskelse i Spring Security. (`authenticated()`-varianten ble ikke kjørt.)
> - Sp4: alt avvises, inkludert `/public/info`, fordi første treff vinner.
>
> Fasit: `tasks/SecurityConfig.java`, `session4-solution`.

---

## Oppgave 4.3: Passord

**Mål:**
1. Erstatt `{noop}` med en `PasswordEncoder`-bean, og lagre bare hashede passord.
2. Skriv ut `encoder.encode("password")` to ganger (for eksempel med en `log.info` i bruker-beanen). Ser de to hashene like ut? Gir `encoder.matches("password", hash)` `true` for begge?

**Hvorfor:** et passord som kan leses i en konfigfil eller database, er et sikkerhetsbrudd som venter på å skje. Hashing er ikke kryptering: det finnes ingen nøkkel og ingen vei tilbake. Du sammenligner hasher.

**Slik gjør du det:**
- `@Bean PasswordEncoder passwordEncoder()` som returnerer `PasswordEncoderFactories.createDelegatingPasswordEncoder()`.
- Ta inn encoderen som parameter i `UserDetailsService`-beanen, og kall `encoder.encode("password")`.
- Se på starten av hashen.

**Spørsmål:**
- Hvorfor er de to hashene forskjellige når passordet er det samme, og hvorfor virker `matches` likevel?
- Hvorfor er BCrypt bevisst treg? Hvorfor er `SHA-256(password)` en dårlig idé?
- Hva gjør `{bcrypt}`-prefikset mulig?

**Ferdig når:** collectionen fortsatt går gjennom (ingenting endres for klienten), og du kan svare på spørsmålene.

> **🔎 For validering:** hashen ser ut som `{bcrypt}$2a$10$...` *(formatet, ikke kopiert fra en kjøring)*. To hasher av samme passord er forskjellige fordi BCrypt legger til et tilfeldig **salt**; `matches` leser saltet ut av hashen. `{bcrypt}`-prefikset lar den delegerende encoderen verifisere gamle hasher med én algoritme og lage nye med en annen, slik at passord kan migreres. Ren SHA-256 er rask, så en angriper prøver milliarder av gjetninger i sekundet, og uten salt gir like passord like hasher.

---

## Oppgave 4.4: CSRF og sesjoner

**Mål:**
1. Med profilen `trace`, send request **09** (POST som admin). Du får `403`, selv om `admin` har riktig rolle. Finn den eksakte årsaken i loggen.
2. Se på response-headerne til request **03** (ingen innlogging, `401`). Finn `Set-Cookie`.
3. Skru av CSRF **og** gjør API-et stateless. Sjekk at **09** og **12** nå går gjennom, og at `Set-Cookie` er borte.

**Hvorfor:** standardinnstillingen beskytter en nettleserapplikasjon som bruker en sesjonscookie. Dette API-et bruker ikke cookies. Du skal kunne forklare hvorfor det er riktig å skru av CSRF her, og hva du må være forsiktig med.

**Slik gjør du det:**
- Logg: søk etter `CSRF`. Se på linjen etter `Did not find a CSRF token in`.
- I `SecurityFilterChain`:
  - `.csrf(AbstractHttpConfigurer::disable)`
  - `.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))`

**Spørsmål:**
- Hva er angrepet? Hvem sender den forfalskede requesten, og hvorfor tror serveren på den?
- Hvorfor trenger ikke en `GET` et CSRF-token?
- Hvorfor er det greit for dette API-et å skru av CSRF? Under hvilken betingelse ville det **ikke** vært greit?
- `STATELESS` betyr at Spring Security aldri oppretter eller bruker en `HttpSession`. Hva betyr det for klienten på den andre requesten?

**Ferdig når:** requestene 09 og 12 går gjennom, ingen `JSESSIONID`-cookie settes, og du kan forklare de to første spørsmålene for sidemannen.

> **🔎 For validering** *(målt)*:
> - Logg: `Did not find a CSRF token in the [X-CSRF-TOKEN] request header`, så `... in the [_csrf] request parameter`, så `Invalid CSRF token found for http://localhost:8080/api/companies` og `Responding with 403 status code`.
> - Før fiksen har `401`-en fra request 03 `Set-Cookie: JSESSIONID=...; HttpOnly`. Etter `STATELESS`: ingen cookie.
> - Etter fiksen: 09 → 201, 12 → 204.
> - Sp. om betingelser: nettleseren legger ved cookies (og cachede Basic-credentials) **automatisk** på en forfalsket request fra en annen side. Det er trygt å skru av CSRF når credentials legges på requesten med vilje av din egen kode, for eksempel et `Bearer`-token i `Authorization`-headeren (økt 5) eller en klient som ikke er en nettleser. Med `Basic`-auth og en nettleser kan nettleseren cache credentials og sende dem på nytt, så argumentet er svakere. Dette er en god diskusjon til gjennomgangen.
> - STATELESS: klienten må sende credentials på **hver** request.
> - Felle: i **standard**kjeden (før 4.2) gir en POST uten CSRF-token **401** i stedet for 403, fordi 403-en går via `/error`, som ikke er tillatt (målt). Med din egen kjede og `/error` i `permitAll()` ser du den ekte 403-en.

---

## Oppgave 4.5: CORS

**Mål:**
1. Start testsiden fra mappen `tools/cors-test` på port 3000 og åpne <http://localhost:3000>:

   ```
   jwebserver -p 3000 -d tools/cors-test
   ```

   (`jwebserver` følger med JDK-en. Hvis den ikke er på PATH, bruk `python -m http.server 3000 --directory tools/cors-test`.)
2. Klikk **GET /api/companies** (bruker `user`, passord `password`). Kallet feiler. Åpne fanene *Console* og *Network* og finn ut hva nettleseren sier, og hvilken request den sendte først.
3. Åpne det samme kallet i Postman (request **04**). Det virker. Hvorfor?
4. Konfigurer CORS: bare origin `http://localhost:3000`, metodene `GET`, `POST`, `DELETE`, headerne `Authorization` og `Content-Type`, for `/api/**`. Sjekk med siden og med Postman-requestene **13** og **14**.

**Hvorfor:** «CORS-feil» er blant de vanligste problemene for en utvikler som bygger en frontend mot et API. Du skal vite hvem som blokkerer, og hvem som ikke gjør det.

**Slik gjør du det:**
- `CorsConfig` finnes som en tom stub i `tasks/`. Gjør den til en `@Configuration` med en `@Bean CorsConfigurationSource` (bruk `CorsConfiguration` og `UrlBasedCorsConfigurationSource`).
- Slå den på i `SecurityFilterChain`: `.cors(Customizer.withDefaults())`. Den finner beanen din.
- Postman-request **13** og **14** sender `Origin`-headeren manuelt og bruker metoden `OPTIONS`. Se på response-headerne `Access-Control-Allow-*`.

**Spørsmål:**
- Hvilken request er «preflighten», og hvorfor sender nettleseren den for dette kallet?
- Beskytter CORS API-et ditt mot `curl` eller Postman? Mot en annen nettside som sender en skjema-`POST`?
- Fjern `Authorization` fra de tillatte headerne. Hva skjer på siden?

**Ferdig når:** siden kan gjøre GET, og POST/DELETE som `admin`; requestene 13 og 14 går gjennom; og du kan svare på de to første spørsmålene.

> **🔎 For validering** *(målt med curl, ikke i en nettleser)*:
> - Uten CORS-konfigurasjon (ingen `.cors(...)` i kjeden, slik den ser ut etter 4.4) gir preflighten (`OPTIONS` uten innlogging, med `Origin`, `Access-Control-Request-Method`) **401**. Med `.cors(Customizer.withDefaults())` men uten `CorsConfigurationSource`-bean gir den **200**, men uten noen `Access-Control-Allow-*`-header. I begge tilfeller mangler `Access-Control-Allow-Origin` (også på et vanlig kall med `Origin`), så nettleseren viser en CORS-feil i Console og selve kallet når aldri frem til siden.
> - Med konfigurasjonen: preflight fra `http://localhost:3000` → **200** med `Access-Control-Allow-Origin: http://localhost:3000`, `Access-Control-Allow-Methods: GET,POST,DELETE`, `Access-Control-Allow-Headers: authorization, content-type`. Fra `http://evil.example` → **403**.
> - Preflighten sendes fordi kallet har en `Authorization`-header (og, for POST, `Content-Type: application/json`): det er ikke en «enkel» request.
> - CORS håndheves av **nettleseren**. Postman og curl bryr seg ikke. Og det hindrer ikke at en skjema-`POST` fra en annen side blir *sendt* (det hindrer bare den andre siden i å *lese* svaret). Derfor erstatter ikke CORS CSRF-beskyttelse.
> - Uten `Authorization` i de tillatte headerne avvises preflighten, og siden viser det blokkerte kallet *(logikk, ikke kjørt i en nettleser)*.
> - Den eksakte ordlyden i nettleserfeilen og utseendet på Network-fanen avhenger av nettleseren: sjekk det under øving.
>
> Fasit: `tasks/CorsConfig.java` og `.cors(...)` i `tasks/SecurityConfig.java`, `session4-solution`.

---

## Oppgave 4.6: Ekstra (valgfritt)

**Mål:** velg én eller flere:
- (a) Skriv `RequestLoggingFilter`: et `OncePerRequestFilter` som logger metode, sti, brukernavn og status for hver request. Registrer det i kjeden **etter** `BasicAuthenticationFilter` med `addFilterAfter(...)`. Sjekk plasseringen med `FilterChainPrinter`.
- (b) Skriv en test med `MockMvc` og `httpBasic("user", "password")` eller `@WithMockUser(roles = "USER")` som sjekker at `user` får `403` på `POST /api/companies`. (`spring-boot-starter-security-test` og `spring-boot-starter-webmvc-test` er allerede avhengigheter. På Spring Boot 4 ligger `@AutoConfigureMockMvc` i `org.springframework.boot.webmvc.test.autoconfigure`.)
- (c) Legg til `@EnableMethodSecurity` og beskytt `CompanyService.delete(...)` med `@PreAuthorize("hasRole('ADMIN')")`. Fjern deretter `DELETE`-regelen fra kjeden, og sjekk at resultatet er det samme.
- (d) Se på alle response-headerne fra API-et (`X-Content-Type-Options`, `Cache-Control`, `X-Frame-Options`). Hvem legger dem til? (Hint: `HeaderWriterFilter`.)

**Hvorfor:** (a) viser hvor lite et filter er, og at plassen i kjeden betyr noe. (b) er hvordan du beskytter reglene dine mot regresjon. (c) er det andre nivået du kan beskytte på: metoden, ikke URL-en.

> **🔎 For validering:** (a) og (b) er implementert i `session4-solution` (`tasks/RequestLoggingFilter.java`, `SecurityRulesTest`), og målt. (c) og (d) er **ikke** implementert eller kjørt. I (a): filteret må **ikke** være en `@Component`, ellers registrerer Spring Boot det også som et vanlig servlet-filter, og det kjører to ganger (det er derfor fasiten oppretter det med `new`). Målt logg: `GET /api/companies/000 user=user status=404`, `GET /nothing user=anonymous status=401`.

---

## 🔎 Oppsummering: hva som ikke er sjekket

- Alt i IntelliJ-GUI-et (jeg kjørte via Maven og `java -jar`).
- Nettleserdelen av oppgave 4.5 og demo D4 (CORS-feilteksten, Network-fanen). HTTP-oppførselen er målt med curl.
- Postman-collectionen er ikke importert i Postman. De samme requestene ble kjørt med curl og som MockMvc-tester (`SecurityRulesTest`, 9 tester, alle grønne på Spring Boot 4.1.2), så de forventede statusene stemmer, men selve JSON-importen er ikke testet.
- Oppgave 4.6 (c) og (d).
