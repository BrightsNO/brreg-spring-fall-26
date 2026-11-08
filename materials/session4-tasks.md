# Økt 4: oppgaver

Du skal selv bruke verktøyene fra demoene, og bygge sikkerhetskonfigurasjonen til API-et steg for steg. Hver oppgave har **Mål** (hva), **Hvorfor** (hva du lærer), **Slik gjør du det** og **Ferdig når**.

---

## Før du starter

1. Ha JDK 25, IntelliJ, Maven og Postman.
2. Branchen har øktnummer i navnet, slik at den ikke kolliderer med branchene fra de andre øktene:

   ```
   git checkout -b my-answers-4 session4-start
   ```

   **Hvorfor en branch:** du skal endre kode, og vil kunne sammenligne med fasiten uten å miste noe.
3. Åpne mappen `session-4/` (ikke repo-roten) i IntelliJ. Maven-vinduet skal vise `company-session4` (høyreklikk `pom.xml` → *Maven → Sync Project* ved behov).
4. Start `CompanyApplication` (grønn pil). Se etter `Using generated security password: ...` i konsollen.
5. Importer `postman/session4.postman_collection.json` i Postman (*Import*). Collectionen har variablene `baseUrl`, `user`, `userPassword`, `admin` og `adminPassword`.
6. Send request **01 Public info**. Du skal få `401`. Ja, selv det «offentlige» endpointet er låst. Det er der vi starter.

Verktøyene du skal bruke:
- **Postman**: collectionen, og *Authorization*-fanen (type *Basic Auth*).
- **TRACE-logg**: start med profilen `trace` (*Run → Edit Configurations → Active profiles*: `trace`). Gir én linje for hvert filter requesten går gjennom.
- **Nettleserens utviklerverktøy** (F12), fanene *Console* og *Network*: til oppgave 4.5.

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

---

## Oppgave 4.6: Ekstra (valgfritt)

**Mål:** velg én eller flere:
- (a) Skriv `RequestLoggingFilter`: et `OncePerRequestFilter` som logger metode, sti, brukernavn og status for hver request. Registrer det i kjeden **etter** `BasicAuthenticationFilter` med `addFilterAfter(...)`. Sjekk plasseringen med `FilterChainPrinter`.
- (b) Skriv en test med `MockMvc` og `httpBasic("user", "password")` eller `@WithMockUser(roles = "USER")` som sjekker at `user` får `403` på `POST /api/companies`. (`spring-boot-starter-security-test` og `spring-boot-starter-webmvc-test` er allerede avhengigheter. På Spring Boot 4 ligger `@AutoConfigureMockMvc` i `org.springframework.boot.webmvc.test.autoconfigure`.)
- (c) Legg til `@EnableMethodSecurity` og beskytt `CompanyService.delete(...)` med `@PreAuthorize("hasRole('ADMIN')")`. Fjern deretter `DELETE`-regelen fra kjeden, og sjekk at resultatet er det samme.
- (d) Se på alle response-headerne fra API-et (`X-Content-Type-Options`, `Cache-Control`, `X-Frame-Options`). Hvem legger dem til? (Hint: `HeaderWriterFilter`.)

**Hvorfor:** (a) viser hvor lite et filter er, og at plassen i kjeden betyr noe. (b) er hvordan du beskytter reglene dine mot regresjon. (c) er det andre nivået du kan beskytte på: metoden, ikke URL-en.
