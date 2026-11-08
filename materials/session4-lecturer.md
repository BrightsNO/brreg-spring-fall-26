# Økt 4: foreleserens manus (steg, hva og hvorfor)

Dette er stegene du gjør foran salen, i rekkefølge. For hvert steg får du **hva** du gjør, **hva som skjer**, og **hvorfor** vi gjør det (budskapet til deltakerne).

Verdier merket *(målt)* er observert ved kjøring av prosjektet (Spring Boot 4.1.2 med Spring Security 7.1.2 og Tomcat 11.0.27, kjørt på Temurin JDK 25.0.5 via Maven, kall gjort med curl og MockMvc). Punkter merket *(ikke sjekket)* har jeg ikke fått kjørt i IntelliJ-GUI-et, i Postman eller i en nettleser.

Økt 4 går fra 08:00 til 10:15 (innsjekk og slides 45 min, deretter oppgaver og 15 min felles gjennomgang), deretter 15 min pause. Dagsplanen finnes i `materials/day2-schedule.md`.

Økt 4 står på egne ben: det er den første økten på dag 2 og avhenger ikke av noe fra dag 1. Deltakerne trenger bare å kunne det grunnleggende om en REST-controller i Spring Boot.

---

## Den røde tråden

Deltakerne kan allerede skrive et REST-API i Spring Boot. Poenget med økten er å vise dem at Spring Security **ikke er magi**:

1. D1: én avhengighet låser alt. Det finnes et standardoppsett, og du kan lese hva det er.
2. D2: sikkerhet er en liste med servlet-filtre foran controlleren. Vi kan skrive den ut, lese den i loggen og se den i call stacken i debuggeren.
3. D3: våre egne regler er noen få linjer Java. 401 betyr «jeg kjenner deg ikke», 403 betyr «jeg kjenner deg, og svaret er nei».
4. D4: CSRF og CORS er de to tingene som «knekker» et API første gang en nettleser er involvert. Vi ser begge feile, leser hvorfor, og fikser dem.

Aha-øyeblikket: *«Sikkerhet er en kjede av filtre som jeg kan skrive ut, lese og endre.»*

---

## Før du starter (dagen før og 10 min før økten)

1. Åpne prosjektet `company-session4` i IntelliJ. Maven-vinduet skal vise prosjektet (høyreklikk `pom.xml` → *Maven → Sync Project* hvis ikke). SDK og language level skal være 25.
2. Lag en egen branch for demoen, så du kan forkaste alle endringer etterpå:

   ```
   git checkout -b demo session4-demo-done
   ```

   **Hvorfor:** demo-taggen har alt klart (demokonfigurasjonene slås på med Spring-profiler, så du trenger ikke skrive mye). Taggen `session4-demo-done` er nødplanen din hvis noe går galt.
3. Last inn Maven på nytt etter at du har byttet tag/branch (*Sync Project*).
4. Lag run configurations i IntelliJ fra `CompanyApplication`, én per profil, med profilen i *Active profiles*: `open`, `trace`, `d3`, `d3,trace`, `d4`, `d4,d4-cors`, og én uten profil. Gi dem tydelige navn (for eksempel «D1 default», «D4 cors»). **Hvorfor:** live demo = ingen skriving av profilnavn.
5. Importer `postman/session4.postman_collection.json` i Postman. Sjekk variablene (`baseUrl` = `http://localhost:8080`).
6. Kjør alle fire demoene én gang på forhånd.
7. Ta skjermbilder som plan B: filterlisten, CSRF-linjene i loggen og CORS-feilen i nettleserens Console (filterlisten og CSRF-linjene er også slide 16 og 17).
8. Port 8080 og port 3000 må være ledige.
9. Åpne en terminal i prosjektmappen og gjør klar webserveren til D4 (ikke start den ennå):

   ```
   jwebserver -p 3000 -d tools/cors-test
   ```

   `jwebserver` ligger i `bin`-mappen til JDK-en. Hvis den ikke er på PATH, bruk full sti, eller `python -m http.server 3000 --directory tools/cors-test`.
10. Bruk stor skrift i IDE, konsoll, Postman og nettleser (Ctrl +). Tøm konsollen før hver demo.

---

## D1. Låst som standard (3 min)

**Mål:** vise hva én avhengighet gjør, og at standarden er «låst», før vi endrer noe.

### Steg 1: den åpne applikasjonen

**Hva:** kjør **«D1 open»** (profil `open`). Send **03 List companies, no login** i Postman.
**Forventet** *(målt)*: `200` og de to selskapene.
**Hvorfor:** utgangspunktet. Slik ser deltakernes applikasjoner fra dag 1 ut: alt er åpent. Profilen slår av autokonfigurasjonen til Spring Security (`application-open.yml`), som er det samme som å ikke ha avhengigheten.

### Steg 2: med Spring Security

**Hva:** stopp. Kjør **«D1 default»** (ingen profil). Send request 03 igjen.
**Forventet** *(målt)*: `401`. Konsollen viser `Using generated security password: <uuid>` og `This generated password is for development use only`.
**Det du sier:** «Vi har ikke skrevet én linje sikkerhetskode. Spring Boot så `spring-boot-starter-security` på classpath og låste alt.»
**Hvorfor:** samme lærdom som på dag 1: classpath bestemmer hva du får. Vis `spring-boot-starter-security` i `pom.xml`.

### Steg 3: logg inn

**Hva:** i Postman, *Authorization → Basic Auth*, bruker `user`, passordet fra konsollen. Send. Send deretter `GET /public/info` og `GET /actuator/health` uten innlogging.
**Forventet** *(målt)*: 03 med passordet → `200`. `/public/info` → **401** (den er ikke «public» ennå: ingen har sagt det). `/actuator/health` → **200** (Spring Boot åpner den som standard i denne versjonen, en overraskelse det er verdt å påpeke).
**Hvorfor:** standarden er «avvis, med mindre autentisert». Ingenting er offentlig før vi sier det (D3).

### Steg 4: nettleseren (valgfritt, 30 s)

**Hva:** åpne `http://localhost:8080/api/companies` i nettleseren.
**Forventet:** en redirect (`302`) til en generert innloggingsside *(målt med curl med `Accept: text/html`; selve nettleseren ikke sjekket)*. Postman får `401` med `WWW-Authenticate: Basic realm="Realm", charset="UTF-8"`.
**Hvorfor:** standardoppsettet støtter både form login og HTTP Basic, og velger ut fra hva klienten ber om. Forberedelse til oppgave 4.1.

**Plan B:** hopp over steg 4. Hvis passordet er vanskelig å kopiere, gå videre og forklar.

---

## D2. Under panseret: TRACE-logg og debuggeren (4 min)

**Mål:** vise *hvor* Spring Security gjør jobben sin: en liste med filtre, i rekkefølge. Både i loggen og i call stacken.

### Steg 1: TRACE-logg

**Hva:** kjør **«D2 trace»** (profil `trace`). I `application-trace.yml` er standardbrukeren satt fast til `user` / `password` med rollen `USER` (kun for denne demoen). Send **04 List companies as user** i Postman med Basic Auth `user` / `password`. Søk (Ctrl+F) etter `Securing GET` i konsollen.
**Forventet** *(målt, under profilen `d3` som har 12 filtre; standardkjeden har 17, så tallene i din logg blir `(n/17)`)*:

```
FilterChainProxy        : Securing GET /api/companies
FilterChainProxy        : Invoking SecurityContextHolderFilter (3/12)
FilterChainProxy        : Invoking CsrfFilter (5/12)
CsrfFilter              : Did not protect against CSRF since request did not match IsNotHttpMethod [TRACE, HEAD, GET, OPTIONS]
FilterChainProxy        : Invoking BasicAuthenticationFilter (7/12)
BasicAuthenticationFilter : Found username 'user' in Basic Authorization header
ProviderManager         : Authenticating request with DaoAuthenticationProvider (1/1)
DaoAuthenticationProvider : Authenticated user
BasicAuthenticationFilter : Set SecurityContextHolder to UsernamePasswordAuthenticationToken [Principal=...User [Username=user ...
FilterChainProxy        : Invoking ExceptionTranslationFilter (11/12)
FilterChainProxy        : Invoking AuthorizationFilter (12/12)
AuthorizationFilter ...  : Authorizing GET /api/companies
FilterChainProxy        : Secured GET /api/companies
```

(Noen `Invoking`-linjer er utelatt her.) Les ovenfra og ned og pek på: *filtrene kalles i rekkefølge*, `BasicAuthenticationFilter` *autentiserer*, `AuthorizationFilter` *bestemmer*. `AuthorizationFilter` er sist.
**Det du sier:** «Dette er hele sikkerhetshistorien til én request. Sytten filtre, i rekkefølge.»
**Hvorfor:** TRACE-loggen er verktøyet deltakerne kommer til å bruke for å finne ut hvorfor de fikk 401/403.

### Steg 2: call stacken

**Hva:** åpne `web/InfoController.java`, sett et breakpoint på første linje i `me(...)`. Kjør **«D2 trace»** med **Debug**. Send **05 Who am I (user)** i Postman med Basic Auth `user` / `password`.
**Hva som skjer:** IntelliJ stopper i `me`.
**Viktig:** IntelliJ skjuler bibliotek-frames som standard (klikk på raden «hidden frames» for å folde dem ut, som i økt 1).
**Forventet** *(målt ved å skrive ut stack tracen i `me`; toppen av stacken først, under profilen `d3`. I debuggeren dukker de samme klassene opp, med noen `ObservationFilterChainDecorator`-frames mellom hvert par)*:

```
InfoController.me
 ... Spring MVC (DispatcherServlet.doDispatch ...)
 ... Tomcat (ApplicationFilterChain ...)
FilterChainProxy ... (i Spring Security 7 via CompositeFilterChainProxy og ServletRequestPathFilter)
AuthorizationFilter.doFilter
ExceptionTranslationFilter.doFilter
AnonymousAuthenticationFilter.doFilter
SecurityContextHolderAwareRequestFilter.doFilter
RequestCacheAwareFilter.doFilter
BasicAuthenticationFilter.doFilterInternal
LogoutFilter.doFilter
CsrfFilter.doFilterInternal
HeaderWriterFilter.doFilterInternal
SecurityContextHolderFilter.doFilter
WebAsyncManagerIntegrationFilter.doFilterInternal
DisableEncodeUrlFilter.doFilterInternal
```

**Det du sier:** «Call stacken *er* filterkjeden, opp ned. Det siste filteret ligger øverst, fordi hvert filter kaller det neste. Controlleren din kalles av det siste.»
**Hvorfor:** det beviser at filterlisten er vanlig Java, ikke magi.

### Steg 3: SecurityContext

**Hva:** klikk på øverste frame (`me`). Skriv `SecurityContextHolder.getContext().getAuthentication()` i *Evaluate expression* (Alt+F8). Fold den ut: `principal`, `authorities`.
**Forventet** *(logikk, ikke kjørt i debuggeren)*: en `UsernamePasswordAuthenticationToken` med principal `user` og authorities `FACTOR_PASSWORD` og `ROLE_USER`. `FACTOR_PASSWORD` er nytt i Spring Security 7: den sier *hvordan* brukeren logget inn (med passord), og er ikke en rolle. Controlleren fikk det samme objektet som parameteren `authentication`.
**Hvorfor:** dette er «hvem er jeg» for gjeldende request. `/api/me` returnerer det samme (målt: `{"name":"user","authorities":["FACTOR_PASSWORD","ROLE_USER"]}`). Fortsett (F9).

**Plan B:** hvis debuggeren er treg, bruk slide 16 (filterlisten) og TRACE-loggen. Steg 1 er kjernen.

---

## D3. Våre egne regler (4 min)

**Mål:** vise at vår egen sikkerhetskonfigurasjon er noen få linjer, og hva 401 og 403 betyr.

### Steg 1: les konfigurasjonen

**Hva:** åpne `demo/D3SecurityConfig.java` og `demo/DemoUsers.java`. Pek på:

| Del | Det du sier |
|---|---|
| `authorizeHttpRequests` | «Reglene sjekkes i rekkefølge. Første treff vinner.» |
| `permitAll()` for `/public/**`, `/actuator/health`, `/error` | «Offentlig. `/error` også, se oppgave 4.2.» |
| `hasAnyRole("USER","ADMIN")` for GET, `hasRole("ADMIN")` for POST/DELETE | «Roller per metode.» |
| `anyRequest().denyAll()` | «Avvis som standard. Det vi glemte å nevne, er stengt.» |
| `httpBasic(...)` | «Hvordan klienten logger inn.» |
| `PasswordEncoder` og `InMemoryUserDetailsManager` (DemoUsers) | «To brukere. Passordene er hashet med BCrypt, aldri lagret som tekst.» |

**Hvorfor:** dette er konfigurasjonen de skal skrive i oppgave 4.2.

### Steg 2: prøv det

**Hva:** kjør **«D3»** (profil `d3`). I Postman: **01** (ingen innlogging), **03** (ingen innlogging), **04** (user), **08** (POST som user), **09** (POST som admin).
**Forventet** *(målt)*:

| Request | Status | Det du sier |
|---|---|---|
| 01 public info | 200 | offentlig |
| 03 list, no login | **401** | «Jeg vet ikke hvem du er» |
| 04 list as user | 200 | |
| 08 POST as user | **403** | «Jeg kjenner deg, og svaret er nei» |
| 09 POST as **admin** | **403** | «Men admin har jo lov! Hvorfor?» (la det ligge, D4 svarer) |

Send også **05 Who am I** som admin og som user: `["FACTOR_PASSWORD","ROLE_ADMIN"]` og `["FACTOR_PASSWORD","ROLE_USER"]`. `hasRole("ADMIN")` ser etter `ROLE_ADMIN`; `FACTOR_PASSWORD` er nytt i Spring Security 7 og viser at innloggingen skjedde med passord (økt 5 og 6 viser hvorfor det betyr noe).
**Hvorfor:** forskjellen på 401 og 403 er det mest nyttige feilsøkingsfaktumet innen sikkerhet. Request 09 er kroken inn til D4.

**Plan B:** hvis du har dårlig tid, gjør bare tabellen (steg 1-2 tar 4 min; gjennomgangen av konfigurasjonen kan kortes ned til `authorizeHttpRequests`-blokken).

---

## D4. CSRF og CORS (5 min)

**Mål:** se de to «mysteriefeilene» fra nettleser-/Postman-verdenen, lese hvorfor, og fikse dem.

### Steg 1: hvorfor 403 for admin?

**Hva:** kjør **«D3 trace»** (profil `d3,trace`). Send request 09 igjen. Søk etter `CSRF` i konsollen.
**Forventet** *(målt)*:

```
Did not find a CSRF token in the [X-CSRF-TOKEN] request header
Did not find a CSRF token in the [_csrf] request parameter
Invalid CSRF token found for http://localhost:8080/api/companies
Responding with 403 status code
```

**Det du sier:** «Spring vil ha et hemmelig token på hver POST/PUT/DELETE. Angriperens side kjenner det ikke. Det er beskyttelsen (slide 12). Den gjelder nettleserøkter med cookies, og API-et vårt har ingen cookies.»
**Hvorfor:** deltakerne leser den faktiske årsaken i loggen. Uten TRACE er dette bare «403, hvorfor?».

### Steg 2: stateless og ingen CSRF

**Hva:** kjør **«D4»** (profil `d4`). Åpne `demo/D4SecurityConfig.java` og pek på de tre nye linjene: `STATELESS`, `csrf(...disable)`, `cors(...)`. Send **09** (POST som admin) og **12** (DELETE som admin).
**Forventet** *(målt)*: `201` og `204`.
Send deretter **03** (ingen innlogging) og se på response-headerne.
**Forventet** *(målt)*: `401` **uten** `Set-Cookie`. Under `d3` har den samme responsen `Set-Cookie: JSESSIONID=...; HttpOnly`.
**Det du sier:** «Stateless: ingenting huskes mellom requestene. Klienten sender credentials hver gang. Det er også derfor CSRF ikke er et problem her (slide 12 forklarer betingelsen).»
**Vær ærlig om nyansen:** med en *nettleser* og *Basic*-auth kan nettleseren cache og sende credentials på nytt. Den rene varianten er et `Bearer`-token som vår egen kode legger på requesten (økt 5).

### Steg 3: CORS feiler i nettleseren

**Hva:** start `jwebserver -p 3000 -d tools/cors-test` i terminalen. Åpne `http://localhost:3000` i nettleseren. Åpne DevTools (F12), fanene *Console* og *Network*. Klikk **GET /api/companies** (siden bruker `user` / `password`).
**Forventet** *(målt med curl; teksten i nettleseren ikke sjekket)*: siden sier at kallet ble blokkert. Console har en rød CORS-melding som nevner `http://localhost:3000` og `Access-Control-Allow-Origin` eller «preflight». Network-fanen viser en `OPTIONS`-request (*preflight*) før GET-en. Med `d4` (`.cors(...)` men ingen CORS-bean) får preflighten `200`, men uten noen `Access-Control-Allow-*`-header, og det vanlige GET-kallet har ikke `Access-Control-Allow-Origin`. Derfor blokkerer nettleseren svaret.
Send deretter det samme kallet i Postman (**04**): det virker.
**Det du sier:** «Serveren svarte. Det var *nettleseren* som nektet å gi svaret til siden. CORS er en nettleserregel. Postman har ingen slik regel.»

### Steg 4: CORS fikset

**Hva:** kjør **«D4 cors»** (profil `d4,d4-cors`). Åpne `demo/D4CorsConfig.java` (tillatt origin, metoder, headere). Last inn siden på nytt og klikk **GET**, logg deretter inn som `admin` / `admin` på siden og klikk **POST** og **DELETE**.
**Forventet** *(målt med curl)*: preflighten fra `http://localhost:3000` → `200` med `Access-Control-Allow-Origin: http://localhost:3000`, `Access-Control-Allow-Methods: GET,POST,DELETE`, `Access-Control-Allow-Headers: authorization, content-type`. Fra `http://evil.example` → `403`. Siden viser `200` med selskapene.
I Network-fanen: `OPTIONS` (200) etterfulgt av `GET` (200).
**Hvorfor:** serveren sier hvem som får *lese* svarene dens; nettleseren håndhever det.

**Plan B:** hvis nettleserdemoen feiler eller tiden er knapp, bruk slide 17 (CSRF i loggen), vis preflighten med Postman-request 13 og 14, og gå rett til oppgavene. Steg 1 og 2 er kjernen; steg 3 og 4 er uansett oppgave 4.5.

---

## Etter demoene: tilbakestill

```
git checkout -- .
git clean -fd
git checkout main
```

(Forkaster alle live-endringer i `demo`-branchen. Taggene røres ikke.) Stopp `jwebserver` med Ctrl+C.

Til oppgavene bruker deltakerne `session4-start`. Til felles gjennomgang bruker du `session4-solution`.

---

## Felles gjennomgang (15 min, `session4-solution`)

```
git checkout session4-solution
```

1. **(4 min) Oppgave 4.2 live.** Åpne `tasks/SecurityConfig.java`. Fjern `"/error"` fra `permitAll()`, start appen, kall `GET /api/companies/000` som `user` og vis at en 404 blir til **403**. Sett den tilbake. Spør: *«Hvor kommer 403-en fra, når controlleren sa 404?»* Svar: en feil i controlleren videresendes til `/error`, og `/error` er sikret som alt annet.
2. **(5 min) CSRF, den ærlige diskusjonen.** Spør: *«Vi skrudde av CSRF. Hvem mener det er OK, og under hvilken betingelse?»* Svar: når nettleseren ikke legger ved credentials av seg selv (Bearer-token satt av vår egen kode, eller ingen nettleser). Nevn at HTTP Basic er det svake punktet i en nettleser. Dette er broen til økt 5.
3. **(3 min) Filterlisten.** La en deltaker lese opp de 14 filtrene fra sin `FilterChainPrinter` og si hva som mangler sammenlignet med de 17 i standardoppsettet (form login, CSRF, standardsider) og hva som er nytt (`RequestLoggingFilter`). Si til dem: *«Hvert `.csrf(...)`-, `.httpBasic(...)`- og `.cors(...)`-kall i konfigurasjonen legger til eller fjerner filtre.»*
4. **(3 min) Spørsmål.** Typiske: *«Hvorfor ikke `WebSecurityConfigurerAdapter`?»* (fjernet i Spring Security 6; en `SecurityFilterChain`-bean er veien å gå.) *«Er HTTP Basic OK i produksjon?»* (bare over HTTPS og for små interne tilfeller; økt 5 og 6 er det egentlige svaret.) *«Hvor kommer brukerne fra i virkeligheten?»* (en database via din egen `UserDetailsService`, eller en ekstern identity provider, økt 6.) *«Trenger jeg `@EnableWebSecurity`?»* (Spring Boot legger den til for deg.)

Bro til økt 5: *«Klienten sender passordet sitt på hver request. Neste steg: logg inn én gang, få et token, og send tokenet i stedet.»*

---

## Det jeg ikke har sett selv (sjekk under øving)

- Alt i IntelliJ-GUI-et (run configurations med profiler, Debug-knappen, Frames, Evaluate expression).
- Den faktiske call stacken i debuggeren i D2 (jeg skrev ut stack tracen fra kode; se D2 steg 2).
- Nettleserdelen av D4: den eksakte CORS-meldingen i Console, utseendet på Network-fanen, redirecten til innloggingssiden i D1 steg 4.
- Postman: import av collectionen, *Authorization*-fanen, Collection Runner. De samme requestene og statusene ble kjørt med curl og i `SecurityRulesTest` (9 tester, grønne på 4.1.2).
- At `/actuator/health` er åpen i standardoppsettet på alle patch-versjoner av Spring Boot. Den var åpen i 4.1.2.

---

## Slides og demoer: når du skal bytte

Presentasjonen er `session4-slides.pptx` (17 slides). Speaker notes på hver slide sier hva du skal si og hvor du skal bytte.

| Tid | Modus | Slides / demo | Min |
|---|---|---|---|
| 08:00 | Slides | 1 Tittel, 2 Agenda, 3 Mål, 4 Oppsettsjekk, 5 Tre verktøy | 15 |
| 08:15 | Slides | 6 Autentisering vs. autorisering, 7 Én avhengighet låser alt | 2 |
| | **Demo 1** (IntelliJ, Postman) | Låst som standard | 3 |
| | Slides | 8 Filterkjeden, 9 Hvem er du? | 3 |
| | **Demo 2** (IntelliJ, Postman) | TRACE-logg og debuggeren | 4 |
| | Slides | 10 Dine egne regler, 11 Passord | 3 |
| | **Demo 3** (IntelliJ, Postman) | Våre egne regler, 401 vs. 403 | 4 |
| | Slides | 12 CSRF, 13 CORS | 3 |
| | **Demo 4** (IntelliJ, Postman, nettleser) | CSRF og CORS | 5 |
| | Slides | 14 Feil du kommer til å møte, 15 Din tur | 2 |
| 08:45 | Oppgaver | deltakerne jobber (slide 15 står oppe) | 75 |
| 10:00 | Felles gjennomgang | `session4-solution` | 15 |
| 10:15 | Pause | | 15 |

Fire runder: slides, demo, slides, demo, slides, demo, slides, demo, slides. Blokken er stram: hvis du ligger etter etter demo 2, kort ned demo 3 til tabellen i steg 2 og kutt D4 steg 3-4 til reserveslidene.
Reserveslides: 16 (filterliste, hvis debuggeren eller TRACE feiler i demo 2) og 17 (CSRF i loggen, hvis demo 4 steg 1 feiler). Feiler nettleserdemoen, vis preflighten med Postman-request 13 og 14.
