# Økt 5: oppgaver

Du skal bygge innloggingssteget og token-sjekken i API-et, steg for steg. Hver oppgave har **Mål** (hva), **Hvorfor** (hva du lærer), **Slik gjør du det** og **Ferdig når**.

Dokumentet har to typer innhold:
- Vanlig tekst er oppgaveteksten deltakerne får.
- Bokser merket **🔎 For validering** er for deg som validerer oppsettet. De fjernes før oppgavene deles ut.

---

## Før du starter

1. Ha JDK 25, IntelliJ, Maven og Postman. Du trenger ikke dine egne svar fra økt 4: start-taggen har det ferdige oppsettet fra økt 4.
2. Branchen har øktnummer i navnet, slik at den ikke kolliderer med branchene fra de andre øktene:

   ```
   git checkout -b my-answers-5 session5-start
   ```

   **Hvorfor en branch:** du skal endre kode, og vil kunne sammenligne med fasiten uten å miste noe.
3. Åpne mappen `session-5/` (ikke repo-roten) i IntelliJ. Maven-vinduet skal vise `company-session5` (høyreklikk `pom.xml` → *Maven → Sync Project* ved behov).
4. Start `CompanyApplication` (grønn pil). I konsollen ser du filterkjeden skrevet ut ved oppstart (`FilterChainPrinter`).
5. Importer `postman/session5.postman_collection.json` i Postman (*Import*). Samlingen har variablene `baseUrl`, `user`, `userPassword`, `admin`, `adminPassword`, `userToken` og `adminToken` (de to siste er tomme fram til oppgave 5.4).
6. Send request **05 List companies, no token**. Du skal få `401`. Send request **02 Login as user**. Du skal få `500`: token-koden er din jobb å skrive.

Verktøyene du skal bruke:
- **Postman**: samlingen, fanen *Authorization* (type *Bearer Token*) og fanen *Scripts* (*Post-response*).
- **jwt.io**: lim inn et token for å lese det. (Tokens er ikke hemmelige for den som har dem, men ikke lim inn ekte produksjonstokens på nettsider.)
- **TRACE-logg**: start med profilen `trace` (*Run → Edit Configurations → Active profiles*: `trace`).

> **🔎 For validering:** `session5-start` skal starte, skrive ut 14 filtre, gi `401` på `GET /api/companies` uten innlogging og `200` med Basic `user` / `password`, og `500` på `POST /auth/login` (stubben kaster en exception). Kodefasiten er `git diff session5-start session5-solution`.

---

## Oppgave 5.1: Utsted et token

**Mål:**
1. Skriv `TokenService.issue(...)` i `tasks/`. Den lager en signert JWT med claimene `sub` (brukernavn), `iat`, `exp` (nå pluss 15 min, fra `properties.ttl()`) og `roles` (rollenavnene til brukeren, for eksempel `["ROLE_USER"]`). Spring Security 7 legger også til en `FACTOR_PASSWORD`-authority etter innlogging. Den sier hvordan brukeren logget inn, og er ikke en rolle: ta bare med authorities som starter med `ROLE_`.
2. Send Postman-request **02 Login as user**. Du skal få `200` og en JSON-body med `access_token`.
3. Lim inn tokenet på <https://jwt.io>. Les headeren og payloaden.
4. Endre `roles` i payloaden til `["ROLE_ADMIN"]` (i jwt.io-editoren). Hva skjer med signaturen?

**Hvorfor:** en JWT er tre Base64-deler. Alle kan lese de to første. Det er signaturen som hindrer hvem som helst i å endre dem.

**Slik gjør du det:**
- `TokenService` har en TODO-kommentar med stegene. `JwtEncoder` og hemmeligheten er allerede satt opp i `AuthConfig` (du bare bruker dem).
- `JwtClaimsSet.builder().subject(...).issuedAt(...).expiresAt(...).claim("roles", ...).build()`
- `JwsHeader.with(MacAlgorithm.HS256).build()`
- `encoder.encode(JwtEncoderParameters.from(header, claims)).getTokenValue()`
- Authorities: `authentication.getAuthorities()`, mappet til `getAuthority()`-strengene sine.

**Spørsmål:**
- Er payloaden hemmelig? Hvem kan lese den?
- Hva betyr `HS256`, og hvem må kjenne hemmeligheten?
- Hva skjer hvis du sender `{"username":"user","password":"wrong"}` til `/auth/login`?

**Ferdig når:** request 02 returnerer `200` med et token, du har lest claimene på jwt.io, og du har sett at signaturen feiler etter endringen.

> **🔎 For validering** *(målt, `session5-solution` og `session5-demo-done`)*:
> - Login-body: `{"access_token":"eyJ...","token_type":"Bearer","expires_in":900}` (fasiten returnerer også et `refresh_token`, se 5.7 a). Header `{"alg":"HS256"}`, payload `{"sub":"user","exp":1793018901,"iat":1793018001,"roles":["ROLE_USER"]}`. `exp - iat = 900`.
> - Feil passord: `401`. `{}` som body: `401`. Ingen body: `400`.
> - Svar: payloaden er bare Base64, så alle som har tokenet, kan lese den. HS256 = HMAC med SHA-256, den samme hemmeligheten signerer og verifiserer. En endret payload stemmer ikke lenger med signaturen (API-et gir `401`, request 14 og en MockMvc-test; at jwt.io viser *Invalid Signature*, er *ikke sjekket i nettleser*).
> - Feil du kommer til å møte: en hemmelighet kortere enn 32 bytes starter fint, men innlogging gir `500` med `KeyLengthException: The secret length must be at least 256 bits`. `expiresAt` som ikke er etter `issuedAt` (for eksempel `app.jwt.ttl=-1m`) gir `500` med `IllegalArgumentException: expiresAt must be after issuedAt`.
>
> Fasit: `tasks/TokenService.java`, `session5-solution`.

---

## Oppgave 5.2: Sjekk tokenet i API-et

**Mål:**
1. I `tasks/SecurityConfig`, bytt ut `.httpBasic(...)` med `.oauth2ResourceServer(...)`, slik at API-et godtar et token i headeren `Authorization: Bearer ...`.
2. Flytt `RequestLoggingFilter` slik at det kommer etter `BearerTokenAuthenticationFilter`.
3. Start på nytt og les filterlisten som skrives ut ved oppstart.
4. Logg inn (request 02), kopier tokenet inn i *Authorization*-fanen (*Bearer Token*) på request **06 List companies as user**, og send den.

**Hvorfor:** API-et er nå en *resource server*: det kjenner ikke passord lenger, bare tokens. Sjekken skjer i ett filter, tidlig i kjeden.

**Slik gjør du det:**
- `http.oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))`
- `JwtDecoder`-beanen i `AuthConfig` plukkes opp av `jwt()`. Du sender den ikke inn selv.
- Start med profilen `trace` og send request 06. Søk etter `BearerTokenAuthenticationFilter`.

**Spørsmål:**
- Hvor mange filtre er det i kjeden nå? Hvilket filter erstattet `BasicAuthenticationFilter`? Hvilket nytt filter ba du ikke om?
- Hva returnerer request 05 (uten token), og hva sier `WWW-Authenticate`-headeren?
- Request 06 returnerer sannsynligvis **ikke** `200`. Hva returnerer den, og hva sier loggen? (Ikke fiks det ennå: det er oppgave 5.3.)

**Ferdig når:** du har den nye filterlisten og har funnet grunnen i loggen til det request 06 returnerer.

> **🔎 For validering** *(målt)*:
> - 15 filtre (14 i starttilstanden): `BasicAuthenticationFilter` er erstattet av `BearerTokenAuthenticationFilter` (8), og `RequestLoggingFilter` (9) kommer etter det. Det nye `OAuth2ProtectedResourceMetadataFilter` (7) ble ikke bedt om: det legges til av `oauth2ResourceServer` i Spring Security 7 og svarer på `/.well-known/oauth-protected-resource` (TRACE: `Invoking OAuth2ProtectedResourceMetadataFilter (7/15)`).
> - 05 gir `401` med `WWW-Authenticate: Bearer resource_metadata="http://localhost:8080/.well-known/oauth-protected-resource"` (TRACE: `Did not process request since did not find bearer token`, deretter `Sending AnonymousAuthenticationToken ... to authentication entry point`).
> - 06 med et gyldig token, før 5.3: `403` med `WWW-Authenticate: Bearer error="insufficient_scope", error_description="The request requires higher privileges than provided by the access token.", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"`. TRACE: `Returning no authorities since could not find any claims that might contain scopes`, deretter `Granted Authorities=[FactorGrantedAuthority [authority=FACTOR_BEARER, …]]` (ingen roller, bare beviset på at brukeren brukte et token), deretter `Sending JwtAuthenticationToken ... to access denied handler`.
> - Felle: hvis `/auth/login` mangler i `permitAll()`, gir innlogging `401`.
>
> Fasit: `.oauth2ResourceServer(...)` i `tasks/SecurityConfig.java`, `session5-solution`.

---

## Oppgave 5.3: Roller som claim

**Mål:**
1. Finn ut hvorfor brukeren får `403` med et gyldig token, ved hjelp av TRACE-loggen (`Granted Authorities=[FactorGrantedAuthority [authority=FACTOR_BEARER, …]]`: ingen roller).
2. Få Spring Security til å bygge authorities fra claimet `roles`, uten noe prefiks lagt til. Kall **07 Who am I (user)** og **08 Who am I (admin)**.

**Hvorfor:** som standard leser Spring Security claimet `scope` (eller `scp`) og gjør hver verdi om til en authority med prefikset `SCOPE_`. Tokenet vårt bruker `roles`, med `ROLE_`-prefikset allerede i verdiene. Du bestemmer hvordan et claim blir til en authority.

**Slik gjør du det:**
- En `@Bean JwtAuthenticationConverter` i `SecurityConfig`, med en `JwtGrantedAuthoritiesConverter` der du kaller `setAuthoritiesClaimName("roles")` og `setAuthorityPrefix("")`.
- Spring Security finner beanen for deg. Hvis den ikke gjør det, send den inn til `jwt(jwt -> jwt.jwtAuthenticationConverter(...))`.

**Spørsmål:**
- Hvordan ser `Authentication` ut nå i `/api/me`? Hvilken klasse er det?
- Hva ville `hasRole("ADMIN")` sjekket hvis du hadde brukt standard-converteren og et token med `"scope": "ADMIN"`?
- Hvor får tokenet sine `roles` fra: klienten eller serveren? Hvorfor betyr det noe?

**Ferdig når:** request 06 til 13 består i samlingen (etter at oppgave 5.4 har lagret tokenene), og du kan svare på det tredje spørsmålet.

> **🔎 For validering** *(målt)*: `/api/me` som user: `{"authorities":["FACTOR_BEARER","ROLE_USER"],"type":"JwtAuthenticationToken","name":"user"}`. TRACE etter fiksen: `Looking for scopes in claim roles`. En `JwtAuthenticationConverter`-bean plukkes opp uten mer oppkobling (målt: demoen har ingen `jwt(...)`-tilpasning). Med et `scope`-claim og standard-converteren ville authorities vært `SCOPE_...`, og `hasRole("ADMIN")` ville sett etter `ROLE_ADMIN` (*ikke kjørt*). Rollene kommer fra serveren når den signerer tokenet: klienten kan ikke endre dem uten å ødelegge signaturen. En rolle i et token er gyldig til tokenet utløper, selv om brukeren mister den i databasen.
>
> Fasit: `jwtAuthenticationConverter()` i `tasks/SecurityConfig.java`, `session5-solution`.

---

## Oppgave 5.4: Postman lagrer tokenet

**Mål:**
1. I request **02** og **03**, åpne *Scripts → Post-response* og lagre tokenet i en collection-variabel (`userToken` og `adminToken`).
2. Sjekk at request **06 - 13** bruker `Bearer {{userToken}}` eller `Bearer {{adminToken}}` i *Authorization*-fanen sin.
3. Kjør hele samlingen med Collection Runner. Alle requestene skal bestå (14 til 17 er oppgave 5.5).

**Hvorfor:** å kopiere tokens for hånd skalerer ikke. Et script som lagrer tokenet, gjør samlingen til en test av API-et.

**Slik gjør du det:**

```js
const token = pm.response.json().access_token;
pm.collectionVariables.set("userToken", token);
```

**Spørsmål:**
- Hva er forskjellen på en collection-variabel og en environment-variabel?
- Hvorfor slutter tokenet å virke etter 15 minutter, og hva gjør du da i Postman?

**Ferdig når:** en kjøring av mappen gir grønt på request 01 til 13.

> **🔎 For validering** *(ikke sjekket i Postman: scriptene og importen av samlingen ble ikke kjørt i Postman)*. Fasitsamlingen har scriptene. Alle 17 requestene ble kjørt i rekkefølge av en liten erstatningsrunner (Python, ikke Postman) som leser samlingens JSON, bruker Bearer/Basic-auth-innstillingene, lagrer tokenene slik scriptene gjør, og sammenligner hver status med testen i samlingen: **17 av 17 består**. Pre-request-scriptet `tamperedToken` (request 14) ble implementert på nytt i runneren, ikke kjørt i Postmans sandbox. En collection-variabel bor i samlingen; en environment-variabel bor i det valgte environmentet. Etter 15 minutter sender du request 02/03 på nytt.

---

## Oppgave 5.5: Dårlige tokens

**Mål:** send hver av disse og skriv ned statusen og `WWW-Authenticate`-headeren (bruk TRACE-loggen også):
1. Request **14**: signaturen er endret.
2. Request **15**: tokenet utløp i 2023.
3. Request **16**: et token med `"alg": "none"` og rollen `ROLE_ADMIN`.
4. Request **17**: HTTP Basic, som i økt 4.
5. Et token helt uten `exp` (det er signert med kursets hemmelighet, så det er gyldig for API-et ditt). Send det som Bearer-token og se om det blir godtatt:

   ```
   eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiaWF0IjoxNzkzMDE4MDAxLCJyb2xlcyI6WyJST0xFX1VTRVIiXX0.0UhCYQMh8U9WxKrtkKI7h2zd581hh5S5NHkAnylHydc
   ```
6. Et token som utløp for 30 sekunder siden. Lag et i jwt.io-editoren: lim inn et ferskt token, skriv inn hemmeligheten fra `application.yml` i signaturfeltet, og sett `exp` til et tidspunkt 30 sekunder tilbake i tid (Unix-sekunder).

**Hvorfor:** en resource server må si nei til alt som ikke er nøyaktig det den selv utstedte. Du bør kunne grunnene utenat: det er det første du sjekker når «tokenet ikke virker».

**Spørsmål:**
- Hvilke av 1 til 6 gir `401`, og hvilke blir godtatt? Er noe av resultatet en overraskelse?
- Hvor i loggen ser du grunnen for et utløpt token?
- Hvorfor er `alg: none` et klassisk angrep, og hvorfor feiler det her?
- Hvorfor er et token uten `exp` farlig?

**Ferdig når:** du har en tabell med de seks resultatene og kan forklare de overraskende.

> **🔎 For validering** *(målt med curl og i `SecurityRulesTest`)*:
>
> | # | Resultat | `WWW-Authenticate` |
> |---|---|---|
> | 1 endret signatur | `401` | `error="invalid_token", error_description="An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature"` |
> | 2 utløpt i 2023 | `401` | `... Jwt expired at 2023-11-14T22:28:20Z` |
> | 3 `alg=none` | `401` | `error_description="Unsupported algorithm of none"` |
> | 4 Basic | `401` | `Bearer resource_metadata="http://localhost:8080/.well-known/oauth-protected-resource"` (ingen error) |
> | 5 ingen `exp` | **`200`** | token godtatt: det utløper aldri |
> | 6 utløpt for 30 s siden | **`200`** | godtatt: standard clock skew 60 s |
>
> Alle `invalid_token`-svarene har i tillegg `error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"` og `resource_metadata="http://localhost:8080/.well-known/oauth-protected-resource"` på slutten. Også målt: utløpt for 55 s siden `200`, 62 s siden `401`. Et token som sier HS512: `401`, `Signed JWT rejected: Another algorithm expected, or no matching key(s) found`. `abc` som token: `401`, `Malformed token`. `Authorization: bearer <token>` (små bokstaver) `200`; uten ordet Bearer `401`.
> - TRACE for det utløpte tokenet: `Invoking BearerTokenAuthenticationFilter (8/15)`, `Authenticating request with JwtAuthenticationProvider (1/2)`, `JwtTimestampValidator : Jwt expired at ...`, `Denying authentication since all attempted providers failed`, `Failed to process authentication request`. (En lang stack trace følger i TRACE. Ignorer den.)
> - `alg: none` er et klassisk angrep fordi headeren sier hvordan tokenet skal sjekkes: et bibliotek som stoler på den, ville hoppet over signatursjekken. Vårt avviser det, og decoderen vår godtar bare HS256.

---

## Oppgave 5.6: Roller på metoden, ikke URL-en

**Mål:**
1. Legg til `@EnableMethodSecurity` og beskytt `CompanyService.delete(...)` (eller controller-metoden) med `@PreAuthorize("hasRole('ADMIN')")`.
2. Endre `DELETE`-regelen i kjeden fra `hasRole("ADMIN")` til `authenticated()`.
3. Sjekk request **12** og **13**. Resultatet skal være det samme som før.
4. Legg `@PreAuthorize("hasRole('ADMIN')")` på controller-metoden i stedet, og logg inn som `user`. Hvor kommer `403` fra?

**Hvorfor:** URL-regler ligger på ett sted og er lette å gå gjennom. Metoderegler sitter ved siden av koden de beskytter, og virker også når metoden kalles fra et annet sted enn en web-request. Ekte prosjekter blander de to.

**Spørsmål:**
- Hva skjer hvis du fjerner `DELETE`-regelen helt? (Tenk på `anyRequest().denyAll()`.)
- Hvilket filter håndterer `AccessDeniedException` som kastes av `@PreAuthorize`?

**Ferdig når:** request 12 og 13 fortsatt gir `403` og `204`.

> **🔎 For validering** *(målt i `session5-solution`)*:
> - Med `DELETE` satt til `authenticated()` og `@PreAuthorize("hasRole('ADMIN')")` på `CompanyService.delete(...)`: user `403`, admin `204` (ukjent selskap `404`), uten token `401`. Samme resultater som med URL-regelen.
> - Hvor 403 kommer fra: URL-regelen slipper gjennom (`AuthenticatedAuthorizationManager`), deretter `AuthorizationManagerBeforeMethodInterceptor : Failed to authorize ... CompanyService.delete`, deretter `ExceptionTranslationFilter : Sending JwtAuthenticationToken ... to access denied handler`. `AuthorizationDeniedException` som kastes inne i controller-kallet, vandrer opp til `ExceptionTranslationFilter`, som lager `403`. (Å legge `@PreAuthorize` på controller-metoden i stedet ble **ikke kjørt**; forvent det samme.)
> - Med `DELETE`-regelen helt fjernet: `anyRequest().denyAll()` fanger den. Målt: anonym `401`, user `403`, **admin `403`**.

---

## Oppgave 5.7: Ekstra (valgfritt)

**Mål:** velg én eller flere:
- (a) **Refresh token.** Få `/auth/login` til å returnere et ekstra, langlivet token (`token_use: refresh`, 7 dager), og legg til `POST /auth/refresh` som tar imot det og returnerer et nytt access token på 15 minutter. Sørg for at refresh-tokenet blir **avvist** som Bearer-token på `/api/**`.
- (b) **Test med `jwt()`-post-processoren.** Skriv en MockMvc-test som kaller `GET /api/companies` med `jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))` og sjekker `200`, og én uten authority som sjekker `403`. `spring-security-test` er allerede en dependency.
- (c) **RS256.** Bytt ut den delte hemmeligheten med et generert RSA-nøkkelpar: signer med den private nøkkelen, verifiser med den offentlige (`NimbusJwtDecoder.withPublicKey(...)`). Hva brekker når du starter serveren på nytt, og hvorfor?
- (d) **Kort levetid.** Sett `app.jwt.ttl` til `20s`. Logg inn, vent, og kall API-et. Forklar så hva en klient må gjøre.

**Hvorfor:** (a) er hvordan ekte systemer får lange sesjoner med kortlivede access tokens. (b) er hvordan du beskytter reglene dine mot regresjon, uten å kjøre en innlogging. (c) er broen til økt 6: den som verifiserer, trenger ikke signeringsnøkkelen.

> **🔎 For validering:**
> - (a) implementert og målt (`TokenService.issueRefresh`, `AuthController.refresh`, en validator i `AuthConfig`). Refresh-tokenet har `token_use: refresh` og en levetid på 7 dager (payload `{"sub":"user","exp":1793624017,"token_use":"refresh","iat":1793019217,"roles":["ROLE_USER"]}`). `/auth/refresh` med et gyldig refresh-token: `200` og et nytt access token. Refresh-tokenet som Bearer på `/api/**`: `401`, `A refresh token cannot be used here`. Et access token eller et tuklet token sendt til `/auth/refresh`: `401`. Fasiten laster brukeren på nytt, så endrede roller blir plukket opp. Det er stateless, så et stjålet refresh-token kan ikke trekkes tilbake.
> - (b) implementert i `SecurityRulesTest`: `jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))` gir `200`, ren `jwt()` gir `403`. Merk: `jwt()` hopper over decoderen og converteren, så du må selv oppgi authorities.
> - (c) RS256 **ikke kjørt**.
> - (d) målt med `app.jwt.ttl=20s`: rett etter innlogging `200`, 30 s etter utløp fortsatt `200`, 72 s etter utløp `401`. Klienten må logge inn på nytt, eller bruke et refresh-token.

---

## 🔎 Oppsummering: hva som ikke er sjekket

- Postman: importen av samlingen, scriptene og Collection Runner. De 17 requestene ble kjørt av en erstatningsrunner og som MockMvc-tester (`SecurityRulesTest`, 20 tester, alle grønne, pluss `CompanyApplicationTests`).
- jwt.io i en nettleser (verktøyet brukes fra nettsiden).
- IntelliJ-GUI-et (run configurations, profiler).
- Oppgave 5.5 punkt 6 (jwt.io-editoren med hemmeligheten, deretter endre `exp`) ble ikke prøvd i nettleser. Tokenet uten `exp` i punkt 5 ble sendt til API-et (`200`).
- Oppgave 5.6: `@PreAuthorize` på controller-metoden ble ikke kjørt. Oppgave 5.7 (c) RS256 ble ikke kjørt.
