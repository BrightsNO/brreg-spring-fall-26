# Økt 5: foreleserens manus (steg, hva og hvorfor)

Verdier merket *(målt)* er observert ved kjøring av `session5-demo-done` og `session5-solution` (Spring Boot 4.1.2 med Spring Security 7.1.2, Temurin JDK 25.0.5 og Maven, kall gjort med curl, MockMvc og newman). Punkter merket *(ikke sjekket)* har jeg ikke fått kjørt i IntelliJ-GUI-et, i Postman eller i en nettleser.

Dette er stegene du gjør foran salen, i rekkefølge. For hvert steg får du **hva** du gjør, **hva som skjer**, og **hvorfor** vi gjør det (budskapet til deltakerne).

Økt 5 kommer rett etter økt 4, etter pausen (10:30). Start-taggen har det ferdige oppsettet fra økt 4 (HTTP Basic, BCrypt-brukere, stateless, ingen CSRF, CORS), så ingen trenger sine egne svar fra økt 4.

---

## Den røde tråden

1. D1: en JWT er tre Base64-deler. Alle kan lese den, og ingen kan endre den uten å ødelegge signaturen. (jwt.io)
2. D2: klienten logger inn én gang på `/auth/login` og får et signert token. Serveren signerer det med en hemmelighet. (Login-endepunktet; oppgavene skriver token-koden.)
3. D3: API-et blir en *resource server*: `BearerTokenAuthenticationFilter` erstatter `BasicAuthenticationFilter` og sjekker tokenet. Et gyldig token kan likevel gi **403**, fordi roles-claimet ikke er mappet. Vi leser hvorfor i TRACE-loggen, og fikser det.
4. D4: Postman gjør innloggingen med et script og tester de dårlige tokenene: tuklet med, utløpt, `alg=none`.

Aha-øyeblikket: *«Et token er et signert postkort: alle kan lese det, ingen kan endre det.»*

---

## Tidsplan (les dette først)

Økten er 120 minutter: **10:30-11:00 slides og demoer, 11:00-12:15 oppgaver, 12:15-12:30 felles gjennomgang.** Det er ingen ekstra innsjekkingsblokk, så oppsummering, oppsettsjekk og verktøy (slide 1-5) må passe inn på rundt 7 minutter. Slides og demoer tar deretter rundt 22 minutter. Dette er stramt, slik økt 4 også var. Hvis du ligger etter: kort ned D4 til de tre requestene 14-16, flytt slide 12 (roller på metoden) til «se oppgave 5.6», og bruk backup-slidene i stedet for live TRACE.

---

## Før du starter (dagen før og 10 min før økten)

1. Åpne prosjektet `company-session5` i IntelliJ. SDK og language level 25.
2. Lag en egen branch for demoen, så du kan forkaste alle endringer etterpå:

   ```
   git checkout -b demo session5-demo-done
   ```

   **Hvorfor:** demo-taggen har alt klart (demokonfigurasjonene slås på med Spring-profiler, så du trenger ikke skrive mye). Taggen `session5-demo-done` er nødplanen din hvis noe går galt.
3. Last inn Maven på nytt etter at du har byttet tag/branch (*Sync Project*).
4. Lag run configurations i IntelliJ fra `CompanyApplication`, én per profil, med profilen i *Active profiles*: `d2`, `d3,trace`, `d3,d3-roles,trace`, og én uten profil. Gi dem tydelige navn («D2 login», «D3 resource server», «D3 roles», «Start»). **Hvorfor:** live demo = ingen skriving av profilnavn.
5. Importer `postman/session5.postman_collection.json` i Postman. Sjekk variablene (`baseUrl` = `http://localhost:8080`).
6. Kjør alle demoene én gang på forhånd. Port 8080 må være ledig.
7. Åpne https://jwt.io i en nettleserfane og lim inn D1-tokenet (under), så du ser at siden laster. Ha tokenet i en tekstfil også.
8. Ta skjermbilder som plan B: TRACE-linjene for `Granted Authorities=[FactorGrantedAuthority [authority=FACTOR_BEARER, …]]`, `WWW-Authenticate`-headeren for et utløpt token, og jwt.io med endret payload (TRACE-loggen for et utløpt token er også slide 17).
9. Bruk stor skrift i IDE, konsoll, Postman og nettleser (Ctrl +). Tøm konsollen før hver demo.

**Forskjeller mellom start-taggen og demo-taggen:** demo-taggen endrer én ting i startkoden: `tasks/SecurityConfig` har `@Profile("!d3 & !d3-roles")`, slik at demokjeden i `demo/D3SecurityConfig` kan erstatte den. Stubben `tasks/TokenService` blir stående som den er; `demo/DemoTokenService` (`@Primary`, profilene `d2`, `d3`, `d3-roles`) er den ferdige versjonen.

---

## D1. Et token på jwt.io (2 min)

**Mål:** vise at en JWT bare er tre Base64-deler, at alle kan lese payloaden, og at en endring i payloaden ødelegger signaturen.

Tokenet (et ekte et fra `/auth/login`, *målt*; hemmeligheten er `course-secret-change-me-0123456789abcdef` fra `application.yml`):

```
eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJ1c2VyIiwiZXhwIjoxNzkzMDE4OTAxLCJpYXQiOjE3OTMwMTgwMDEsInJvbGVzIjpbIlJPTEVfVVNFUiJdfQ.QtHBFYFqI6Vr4DZlcEBWI1MzTJpUE2xJH_0UDwCbpHs
```

### Steg 1: les det

**Hva:** lim inn tokenet i jwt.io (venstre side).
**Forventet** *(dekodede verdier målt; selve jwt.io-siden ikke sjekket)*: header `{"alg":"HS256"}`, payload `{"sub":"user","exp":1793018901,"iat":1793018001,"roles":["ROLE_USER"]}`. jwt.io viser `exp` og `iat` som datoer og kan si at tokenet er utløpt.
**Hva du sier:** «Ingen ga jwt.io et passord. Payloaden er bare Base64. Alle som får tak i tokenet, kan lese den.»

### Steg 2: endre payloaden

**Hva:** i payload-editoren, endre `"ROLE_USER"` til `"ROLE_ADMIN"`. Tokenet til venstre endrer seg. Skriv inn hemmeligheten (`course-secret-change-me-0123456789abcdef`) i signaturfeltet.
**Forventet** *(ikke sjekket i nettleser)*: jwt.io sier at signaturen er ugyldig for et token som ikke ble signert med hemmeligheten, og gyldig hvis jwt.io signerer det med hemmeligheten selv. Forklar: editoren kan signere på nytt hvis den kjenner hemmeligheten; **det gjør ikke angriperen**.
**Hvorfor:** signaturen dekker header og payload. Uten hemmeligheten kan du ikke lage et token serveren godtar. API-testen av dette er request 14 (endret signatur) og oppgave 5.5.

**Plan B** (jwt.io er nede eller blokkert): dekod i terminalen *(målt)*:

```
echo "eyJzdWIiOiJ1c2VyIiwiZXhwIjoxNzkzMDE4OTAxLCJpYXQiOjE3OTMwMTgwMDEsInJvbGVzIjpbIlJPTEVfVVNFUiJdfQ==" | base64 -d
```

skriver ut payload-JSON-en.

---

## D2. Innlogging gir et token (3 min)

**Mål:** vise login-endepunktet, og koden som lager tokenet.

### Steg 1: koden

**Hva:** kjør **«D2 login»** (profil `d2`). Åpne `tasks/TokenService.java` (stubben med TODO, slik deltakerne har den) og deretter `demo/DemoTokenService.java` (den ferdige koden). Pek på: `JwtClaimsSet` (subject, issuedAt, expiresAt fra `properties.ttl()`, claimet `roles`), `JwsHeader` med HS256, og `encoder.encode(...)`. Åpne `auth/AuthController.java`: `authenticationManager.authenticate(...)` og så `tokenService.issue(...)`. Åpne `auth/AuthConfig.java`: `JwtEncoder`-beanen (`NimbusJwtEncoder` med `ImmutableSecret`) og hemmeligheten fra `application.yml`.
**Hvorfor:** deltakerne skriver `issue(...)` i oppgave 5.1. `AuthController` og `AuthConfig` er gitt.

### Steg 2: prøv det

**Hva:** Postman **02 Login as user**. Kopier så `access_token` til jwt.io. Deretter **04 Login with wrong password**.
**Forventet** *(målt)*: `200` og bodyen `{"access_token":"eyJ...","token_type":"Bearer","expires_in":900}`. Feil passord gir `401`. Å poste `{}` gir `401`. En post uten body gir `400`. Under profilen `d2` er selve API-et fortsatt beskyttet av Basic: `GET /api/companies` med `user` / `password` gir `200` *(målt)*, og det nye tokenet brukes ikke ennå.
**Hva du sier:** «Passordet sjekkes én gang. Etter dette bruker klienten bare tokenet.»
Fasiten (og Postman-fasitsamlingen) returnerer også et `refresh_token`: ikke vis det i demoen, det hører til ekstraoppgave 5.7 (a) og backup-slide 18.

**Feil du kommer til å møte** *(målt)*: med `APP_JWT_SECRET=short` starter appen, men innlogging gir `500`, og loggen har `KeyLengthException: The secret length must be at least 256 bits`. HS256 trenger 32 bytes.

**Plan B:** vis responsen i Postman-historikken, eller slide 10.

---

## D3. API-et som resource server (4 min)

**Mål:** bytte ut Basic med token-sjekken, se 401 uten token, se den overraskende **403** med et gyldig token, lese hvorfor, og fikse det.

### Steg 1: konfigurasjonen

**Hva:** kjør **«D3 resource server»** (profil `d3,trace`). Åpne `demo/D3SecurityConfig.java`. Pek på: `oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))` i stedet for `httpBasic`, `/auth/login` i `permitAll()`, og `addFilterAfter(new RequestLoggingFilter(), BearerTokenAuthenticationFilter.class)`. Oppstartsloggen lister filtrene.
**Forventet** *(målt)*: **15 filtre**: `DisableEncodeUrlFilter`, `WebAsyncManagerIntegrationFilter`, `SecurityContextHolderFilter`, `HeaderWriterFilter`, `CorsFilter`, `LogoutFilter`, `OAuth2ProtectedResourceMetadataFilter` (7), `BearerTokenAuthenticationFilter` (8), `RequestLoggingFilter` (9), `RequestCacheAwareFilter`, `SecurityContextHolderAwareRequestFilter`, `AnonymousAuthenticationFilter`, `SessionManagementFilter`, `ExceptionTranslationFilter`, `AuthorizationFilter`. I økt 4 hadde kjeden 14, med `BasicAuthenticationFilter` på plass 7.
**Hva du sier:** «Basic ut, Bearer inn. Det ekstra `OAuth2ProtectedResourceMetadataFilter` (plass 7) er nytt i Spring Security 7. Det svarer på `/.well-known/oauth-protected-resource` og forteller klienter hvor tokenet kommer fra. Det gjør ingenting for vanlige kall.» *(målt: `Invoking OAuth2ProtectedResourceMetadataFilter (7/15)` i TRACE-loggen.)*

### Steg 2: 401 uten token, 403 med

**Hva:** Postman **05** (uten token): `401`. Deretter **02** (logg inn som user), kopier tokenet inn i Authorization-fanen (*Bearer Token*) på **06 List companies as user** og send.
**Forventet** *(målt)*: 05 gir `401` med `WWW-Authenticate: Bearer resource_metadata="http://localhost:8080/.well-known/oauth-protected-resource"`. 06 gir **`403`** med `WWW-Authenticate: Bearer error="insufficient_scope", error_description="The request requires higher privileges than provided by the access token.", error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"`.
**Hva du sier:** «Tokenet er gyldig. Vi fikk 403, ikke 401: jeg vet hvem du er, og svaret er nei. Men `user` har jo rollen USER? La oss lese loggen.»

### Steg 3: les loggen

**Hva:** søk etter `Granted Authorities` i konsollen.
**Forventet** *(målt)*:

```
JwtGrantedAuthoritiesConverter : Returning no authorities since could not find any claims that might contain scopes
BearerTokenAuthenticationFilter : Set SecurityContextHolder to JwtAuthenticationToken [Credentials=[PROTECTED], Authenticated=true, Granted Authorities=[FactorGrantedAuthority [authority=FACTOR_BEARER, issuedAt=…]]]
ExceptionTranslationFilter : Sending JwtAuthenticationToken [... Granted Authorities=[FactorGrantedAuthority [authority=FACTOR_BEARER, …]]] to access denied handler since access is denied
```

**Hva du sier:** «Signaturen og utløpstiden er i orden, så brukeren er autentisert. Men authorities har ingen roller: bare `FACTOR_BEARER`, som er Spring Security 7 sin måte å si «brukeren logget inn med et token». Som standard leser Spring Security claimet `scope` (eller `scp`) og legger på prefikset `SCOPE_`. Tokenet vårt har `roles`.»

### Steg 4: fiksen

**Hva:** kjør **«D3 roles»** (profil `d3,d3-roles,trace`). Åpne `demo/D3RolesConfig.java`: `setAuthoritiesClaimName("roles")` og `setAuthorityPrefix("")`. Send **06** igjen, deretter **07 Who am I (user)**.
**Forventet** *(målt)*: `200`, og `/api/me` gir `{"authorities":["FACTOR_BEARER","ROLE_USER"],"type":"JwtAuthenticationToken","name":"user"}`. Loggen har `Looking for scopes in claim roles`. `JwtAuthenticationConverter` er en bean: Spring Security finner den uten mer oppkobling.
**Hvorfor:** dette er oppgave 5.3. Deltakerne skal vite at det er *de* som bestemmer hvilket claim som blir til authorities.

**Plan B:** slide 16 for filterlisten; slide 14 for forklaringen av 403.

---

## D4. Postman: innlogging med script og dårlige tokens (2 min)

**Mål:** vise arbeidsflyten de skal bruke i oppgave 5.4 og 5.5.

**Hva:** med profilen `d3,d3-roles` kjørende, åpne **fasit**-samlingen (foreleserens kopi, med scriptene). Send **02** og **03** (scriptene lagrer `userToken` og `adminToken`: vis fanen *Scripts → Post-response*). Kjør **14** (tuklet med), **15** (utløpt) og **16** (`alg=none`). Åpne Collection Runner og kjør hele mappen.
**Forventet** *(målt med curl; Postman-scriptene er ikke sjekket)*:

| Request | Status | `WWW-Authenticate` |
|---|---|---|
| 14 tuklet signatur | 401 | `error="invalid_token", error_description="An error occurred while attempting to decode the Jwt: Signed JWT rejected: Invalid signature"` |
| 15 utløpt | 401 | `... Jwt expired at 2023-11-14T22:28:20Z` (tokenet i samlingen) |
| 16 `alg=none` | 401 | `error="invalid_token", error_description="Unsupported algorithm of none"` |
| 17 Basic | 401 | `Bearer resource_metadata="http://localhost:8080/.well-known/oauth-protected-resource"` |
| token signert som HS512 | 401 | `Signed JWT rejected: Another algorithm expected, or no matching key(s) found` |
| søppel (`abc`) | 401 | `An error occurred while attempting to decode the Jwt: Malformed token` |

*Alle `invalid_token`-svarene har i tillegg `error_uri="https://tools.ietf.org/html/rfc6750#section-3.1"` og `resource_metadata="http://localhost:8080/.well-known/oauth-protected-resource"` etter `error_description`.* *(målt)*

**Hva du sier:** «Et dårlig token kommer aldri fram til controlleren. Grunnen står i headeren. Og husk: 401 betyr ikke noe gyldig token, 403 betyr et gyldig token med feil rolle.»

**Overraskelser å nevne** *(målt)*:
- Et token som utløp for **30 sekunder siden, blir godtatt** (200), også 55 s siden. 62 s siden blir avvist. Standard clock skew er 60 sekunder.
- Et token **uten `exp`** blir godtatt (200). Sett alltid `exp`.
- Navnet på schemet skiller ikke mellom store og små bokstaver: `Authorization: bearer <token>` gir 200. Uten ordet `Bearer` gir det 401.

**Plan B:** slide 17 (TRACE-loggen for et utløpt token).

---

## Etter demoene: tilbakestill

```
git checkout -- .
git clean -fd
git checkout main
```

(Forkaster alle live-endringer i `demo`-branchen. Taggene er urørt.)

Til oppgavene bruker deltakerne `session5-start`. Til den felles gjennomgangen bruker du `session5-solution`.

---

## Felles gjennomgang (15 min, `session5-solution`)

```
git checkout session5-solution
```

1. **(4 min) Oppgave 5.3 live.** Åpne `tasks/SecurityConfig.java`. Kommenter ut `jwtAuthenticationConverter()`-beanen, start med profilen `trace`, logg inn, og kall `GET /api/companies`: `403` og `Granted Authorities=[FactorGrantedAuthority [authority=FACTOR_BEARER, …]]` (ingen roller) *(målt i demoprofilen)*. Sett den inn igjen. Spør: *«Hvor kommer rollene i tokenet fra?»* Svar: serveren skrev dem ved innlogging. Klienten kan ikke endre dem uten å ødelegge signaturen (en MockMvc-test og request 14 viser `401`). En rolle er gyldig helt til tokenet utløper.
2. **(4 min) Dårlige tokens (5.5).** La en deltaker gå gjennom de seks resultatene (tabell i oppgavearket). Fremhev: utløpt for 30 s siden blir godtatt, 62 s siden blir det ikke (60 s clock skew), og et token uten `exp` blir godtatt *(målt)*.
3. **(3 min) Filterliste.** La en deltaker lese opp de 15 filtrene og si hva som endret seg fra 14 (`BasicAuthenticationFilter` ut, `BearerTokenAuthenticationFilter` inn, pluss `OAuth2ProtectedResourceMetadataFilter`). Si til dem: *«Hvert `.httpBasic(...)`-, `.oauth2ResourceServer(...)`- og `.csrf(...)`-kall i konfigurasjonen legger til eller fjerner filtre.»*
4. **(4 min) Spørsmål.** Typiske: *«Hvor lagrer klienten tokenet?»* (ikke i `localStorage` hvis du kan unngå det: alle script på siden kan lese det. En `HttpOnly`-cookie tar CSRF-spørsmålet fra i formiddag tilbake. Mobil- og serverklienter har det enklere.) *«Hvordan logger jeg ut?»* (du kan ikke trekke tilbake et stateless token; bruk kort levetid, en deny-list eller referansetokens. Refresh-tokenet i 5.7 a er det samme problemet: det trenger state for å kunne trekkes tilbake.) *«HS256 eller RS256?»* (RS256 så fort noen andre verifiserer.) *«Hvorfor `JwtEncoder` i appen, hvem gjør det i virkeligheten?»* (en authorization server: økt 6.) *«Erstatter `@PreAuthorize` URL-reglene?»* (nei: med `DELETE`-regelen helt fjernet blokkerer `denyAll()` fortsatt alle, også admin: målt `403`.)

Bro til økt 6: *«I dag signerte API-et vårt sine egne tokens. Hva om noen andre logger inn brukeren, og API-et vårt bare verifiserer? Det er OAuth2 og en resource server.»*

---

## Det jeg ikke har sett selv (sjekk mens du øver)

- Alt i IntelliJ-GUI-et (run configurations med profiler, de delene av demoene som ikke bruker breakpoints).
- jwt.io i en nettleser: layouten, og den nøyaktige teksten ved siden av en endret payload.
- Postman: importen, fanen *Authorization → Bearer Token*, scriptene og Collection Runner. De samme requestene ble kjørt med curl.

- Fasitens ekstra oppførsel, som **ikke** er med i demoen: `/auth/login` returnerer også et `refresh_token`, og `AuthController` har `/auth/refresh` (oppgave 5.7 a). Demoen og slidene bruker den enklere login-responsen fra start-taggen.
- Oppgave 5.6 med `@PreAuthorize` på controller-metoden (bare service-varianten ble kjørt). Oppgave 5.7 (c) RS256.
- Postman-appen. De 17 requestene i fasitsamlingen ble kjørt med `newman` mot `session5-solution` (17 av 17 requests og 21 av 21 assertions består, scriptene inkludert), og testene i `SecurityRulesTest` (20 tester, alle grønne) dekker de samme tilfellene.
- Tokenet uten `exp` i oppgave 5.5 ble sendt til API-et. Å lage et token med `exp` 30 sekunder tilbake i tid i jwt.io-editoren ble ikke prøvd.

---

## Slides og demoer: når du bytter

Decket er `session5-slides.pptx` (18 slides). Speaker notes på hver slide sier hva du skal si og når du bytter.

| Tid | Modus | Slides / demo | Min |
|---|---|---|---|
| 10:30 | Slides | 1 Tittel (oppsummering av økt 4), 2 Agenda, 3 Mål, 4 Oppsettsjekk, 5 Tre verktøy | 7 |
| 10:37 | Slides | 6 Sesjon eller token, 7 Inni en JWT | 2 |
| | **Demo 1** (nettleser) | Et token på jwt.io | 2 |
| | Slides | 8 Signering, 9 Fallgruver, 10 Innlogging | 3 |
| | **Demo 2** (IntelliJ, Postman) | Innlogging gir et token | 3 |
| | Slides | 11 API-et sjekker bare tokenet | 1 |
| | **Demo 3** (IntelliJ, Postman) | Resource server, 403-overraskelsen, fiksen | 4 |
| | Slides | 12 Roller på URL-en eller metoden, 13 Hva serveren sier til et dårlig token | 2 |
| | **Demo 4** (Postman) | Innlogging med script og dårlige tokens | 2 |
| | Slides | 14 Feil du kommer til å møte, 15 Din tur | 1 |
| 11:00 | Oppgaver | deltakerne jobber (slide 15 blir stående) | 75 |
| 12:15 | Felles gjennomgang | `session5-solution` | 15 |
| 12:30 | Lunsj | | 45 |

Fire rundturer igjen. Summen av minuttene over er 7 + 2 + 2 + 3 + 3 + 1 + 4 + 2 + 2 + 1 = 27, med 3 minutter slakk til overganger.
Backup-slides: 16 (filterliste med Bearer, hvis TRACE-loggen eller `FilterChainPrinter` svikter i demo 3), 17 (TRACE-logg for et utløpt token, hvis demo 4 svikter) og 18 (refresh tokens, hvis noen spør hvordan lange sesjoner fungerer).
