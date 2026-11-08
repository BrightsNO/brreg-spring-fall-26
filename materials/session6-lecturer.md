# Økt 6: foreleserens manus (steg, hva og hvorfor)

Verdier merket *(målt)* er observert ved kjøring av `session6-demo-done` og `session6-solution` på Spring Boot 4.1.2 (Spring Security 7) med JDK 25, med kall fra curl, `newman` og et PKCE-script (`verktoy/hent-token-pkce.py`) i stedet for Postman-fanen. Punkter merket *(ikke sjekket)* har ikke blitt kjørt i IntelliJ-grensesnittet, i Postman-appen eller i en nettleser.

Dette er stegene du gjør foran salen, i rekkefølge. For hvert steg får du **hva** du gjør, **hva som skjer**, og **hvorfor** vi gjør det (budskapet til deltakerne).

Økt 6 er dagens og kursets siste økt. Den følger økt 5 etter lunsj (13:15). Start-taggen har den ferdige koden fra økt 5 (egen innlogging og egne HS256-tokens), så ingen trenger sine egne svar fra før.

---

## Den røde tråden

1. Til nå har API-et vårt logget inn brukeren og signert tokenet selv. I dag gjør noen andre det: en **authorization server**. API-et sjekker bare tokenet.
2. D1: to flyter i Postman. Authorization Code med PKCE for en person, Client Credentials for en maskin. Vi leser begge tokenene.
3. D2: API-et blir en **resource server** med én linje (`issuer-uri`). Det finner de offentlige nøklene selv. Reglene fra økt 5, som bare ser på roller, gir maskinen 403.
4. D3: **scopes og roller**. URL-regelen sjekker scopet (hva appen får gjøre), metoden sjekker rollen (hva brukeren får gjøre). En 2x2-tabell med tokens mot rettigheter.

Aha-øyeblikket: *«API-et logger ingen inn. Det stoler på en utsteder.»*

---

## Tidsplan (les dette først)

Økten er 120 minutter: **13:15-13:45 slides og demoer, 13:45-15:00 oppgaver, 15:00-15:15 felles gjennomgang**, deretter utsjekk kl. 15:15 (15 min). Som i økt 5 er det ingen egen innsjekk, så oppsummering, oppsettsjekk og verktøy (slide 1-5) må passe inn på rundt 7 minutter. Slides og demoer tar deretter rundt 20 minutter. Det er stramt. Hvis du ligger etter: kort ned D1 til Client Credentials pluss det ferdige brukertokenet, bruk backup-slide 17 for Authorization Code, og kort ned D3 til tabellen på slide 13.

---

## Før du starter (dagen før og 10 min før økten)

1. Åpne repoet `company-session6` i IntelliJ. SDK og language level 25. To applikasjoner: `auth-server` (port 9000) og `api` (port 8080).
2. Lag en egen branch for demoen, så du kan forkaste alle endringer etterpå:

   ```
   git checkout -b demo session6-demo-done
   ```

3. Last inn Maven på nytt etter at du har byttet tag/branch (*Sync Project*).
4. Lag run configurations i IntelliJ: **«Auth server»** (`AuthServerApplication`, ingen profil) og for API-et (`CompanyApplication`): **«API start»** (ingen profil), **«D2»** (profil `d2,trace`) og **«D3»** (profil `d3`).
5. Importer `api/postman/session6.postman_collection.json` i Postman. Sjekk variablene (`baseUrl` = `http://localhost:8080`, `authUrl` = `http://localhost:9000`).
6. **Hent brukertokenene før økten** (de lever i 30 minutter, så hent nye rett før): i Postman, *Authorization → OAuth 2.0*, Authorization Code (With PKCE), som i oppgave 6.2 (klient `postman`, scope `openid companies.read companies.write`). Logg inn som `user` og som `admin`, og lagre tokenene i samlingsvariablene `userToken` og `adminToken`. Hent ett admin-token til med bare scopet `companies.read` til `adminReadToken`. **Tøm cookies** mellom innloggingene, fordi innloggingssesjonen hos authorization serveren blir liggende i nettleseren. *(Postman-fanen ikke sjekket.)* Reserve hvis Postman-fanen ikke virker: `python3 verktoy/hent-token-pkce.py user password "openid companies.read companies.write"` skriver ut token-svaret *(målt)*.
7. Kjør alle demoene én gang på forhånd. Port 8080 og 9000 må være ledige.
8. Ta gjerne egne skjermbilder som plan B på maskinen din: OAuth 2.0-fanen med begge grants, API-loggen med `JwtDecoderInitializationException` og TRACE-linjene med `Granted Authorities`.
9. Bruk stor skrift i IDE, konsoll, Postman og nettleser (Ctrl +). Tøm konsollen før hver demo.

**Forskjeller mellom start-taggen og demo-taggen:** `tasks/SecurityConfig` har `@Profile("!d3")`, slik at `demo/D3SecurityConfig` kan erstatte den. Profilene `d2` og `d3` setter også `app.legacy=false` og `issuer-uri` (`application-d2.yml`, `application-d3.yml`). Innloggingskoden fra økt 5 i pakken `legacy` slås av med den egenskapen, fordi HS256-`JwtDecoder`-beanen ellers ville vunnet over den som lages fra `issuer-uri` (se oppgave 6.3).

---

## D1. Tokens i Postman (4 min)

**Mål:** vær klientappen. Hent et token med hver av de to flytene, og les dem.

### Steg 1: se på authorization serveren

**Hva:** start **«Auth server»** og **«API start»**. Åpne `http://localhost:9000/.well-known/openid-configuration` i nettleseren.
**Forventet** *(målt)*: JSON med `issuer` `http://localhost:9000`, `authorization_endpoint` `.../oauth2/authorize`, `token_endpoint` `.../oauth2/token`, `jwks_uri` `.../oauth2/jwks`, `code_challenge_methods_supported` `["S256"]` og `grant_types_supported` med `authorization_code`, `client_credentials`, `refresh_token` og token-exchange. JWKS (`jwks_uri`) har én RSA-nøkkel med en `kid`.
**Det du sier:** «Dette er alt en klient og et API trenger å vite om serveren. ID-porten og Maskinporten publiserer det samme dokumentet.»

### Steg 2: Client Credentials (maskinen)

**Hva:** i Postman, *Authorization → OAuth 2.0*, grant type *Client Credentials*. Access Token URL `http://localhost:9000/oauth2/token`, Client ID `machine`, Client Secret `machine-secret`, Scope `companies.read`, Client Authentication *Send as Basic Auth header*. *Get New Access Token*, og lim tokenet inn på jwt.io. (Eller send forespørsel **02** i samlingen: scriptet lagrer `machineToken`.)
**Forventet** *(målt med curl; Postman ikke sjekket)*: header `{"kid":"<uuid>","alg":"RS256"}`; payload `{"sub":"machine","aud":"machine","nbf":...,"scope":["companies.read"],"iss":"http://localhost:9000","exp":...,"iat":...,"jti":"<uuid>"}`. Levetid 30 minutter (`expires_in: 1799`). **Ingen `roles`.**
**Det du sier:** «Ingen bruker her. Tokenet sier hva maskinen får gjøre.»

### Steg 3: Authorization Code med PKCE (personen)

**Hva:** grant type *Authorization Code (With PKCE)*. Auth URL `http://localhost:9000/oauth2/authorize`, Access Token URL som over, Client ID `postman`, ingen hemmelighet, Callback URL `https://oauth.pstmn.io/v1/callback` (*Authorize using browser*), Code Challenge Method *SHA-256*, Scope `openid companies.read companies.write`. *Get New Access Token*: nettleseren åpnes, logg inn som `user` / `password`. Lim access tokenet inn på jwt.io.
**Forventet** *(målt med PKCE-scriptet; Postman og nettlesersiden ikke sjekket)*: payload `{"sub":"user","aud":"postman","nbf":...,"scope":["companies.read","openid","companies.write"],"roles":["ROLE_USER"],"iss":"http://localhost:9000","exp":...,"iat":...,"jti":"..."}`. Svaret har også et `id_token` (`sub: user`, `aud: postman`, `azp: postman`, `auth_time`, `sid`; uten scope og roller) og `expires_in`. Ikke noe `refresh_token`: serveren gir ikke det til en offentlig klient.
**Det du sier:** «Innloggingen skjedde hos authorization serveren. Postman så aldri passordet. Den fikk en code og byttet den mot et token. PKCE betyr at en stjålet code er verdiløs.»

**Detaljer du kan vise:** en authorize-forespørsel uten `code_challenge` blir sendt til callbacken med `error=invalid_request` og beskrivelsen `OAuth 2.0 Parameter: code_challenge` *(målt)*; maskinen som ber om `companies.write`, får `{"error":"invalid_scope"}` *(målt)*. En feil `code_verifier` gir `{"error":"invalid_grant"}`, det samme gjør en code som er brukt én gang; en `redirect_uri` som ikke er registrert, gir `400` (når du allerede er innlogget; uten innlogging blir du først sendt til påloggingssiden), og en feil klienthemmelighet gir `401` med `{"error":"invalid_client"}` *(målt)*.
**Plan B:** slide 17 (flyten i curl) og de ferdige tokenene i samlingsvariablene.

---

## D2. API-et som resource server (4 min)

**Mål:** én linje konfigurasjon får API-et til å stole på serveren. Se den første overraskelsen: maskinen får 403.

### Steg 1: konfigurasjonen

**Hva:** stopp «API start». Kjør **«D2»** (profil `d2,trace`). Åpne `application-d2.yml`: `issuer-uri: http://localhost:9000` og `app.legacy: false`. Pek på: ingen hemmelighet, ingen `JwtDecoder`-bean. Åpne pakken `legacy` og si: «Dette er all koden fra økt 5: innlogging, signering og hemmeligheten. Den er slått av. I oppgave 6.3 sletter dere den.»
**Forventet** *(målt)*: API-et starter. Filterlisten har **15 filtre**, som i økt 5: `OAuth2ProtectedResourceMetadataFilter` (7) og `BearerTokenAuthenticationFilter` (8).

### Steg 2: brukeren kommer inn

**Hva:** Postman **07 List companies as user** (Bearer `{{userToken}}`).
**Forventet** *(målt)*: `200`. `/api/me` gir `{"type":"JwtAuthenticationToken","name":"user","authorities":["FACTOR_BEARER","ROLE_USER"]}`: rolle-converteren fra økt 5 leser claimet `roles`. `FACTOR_BEARER` legges til av Spring Security 7 og sier bare at brukeren kom inn med et token.
**Det du sier:** «API-et fikk ingen hemmelighet. Spring Boot leste discovery-dokumentet, fant `jwks_uri`, hentet den offentlige nøkkelen og sjekket signatur, utløp og utsteder.»

### Steg 3: maskinen kommer ikke inn

**Hva:** Postman **05 List companies as machine** (Bearer `{{machineToken}}`). Søk i konsollen etter `Granted Authorities`.
**Forventet** *(målt)*: `403`. TRACE: `JwtGrantedAuthoritiesConverter : Looking for scopes in claim roles`, `Granted Authorities=[FactorGrantedAuthority [authority=FACTOR_BEARER, …]]` og `Sending JwtAuthenticationToken ... to access denied handler since access is denied`. Pek på at lista ikke har noen `ROLE_`- eller `SCOPE_`-authority.
**Det du sier:** «Et gyldig token, og likevel 403. Reglene våre ser bare på roller, og en maskin har ingen. Samme lærdom som i 5.3. Det er det scopes er til (slide 13).»

### Steg 4 (valgfritt, 1 min): når serveren er nede

**Hva:** stopp authorization serveren, og **start API-et på nytt**. Send 07.
**Forventet** *(målt)*: `500`, og API-loggen har `JwtDecoderInitializationException: Failed to lazily resolve the supplied JwtDecoder instance` med `Unable to resolve the Configuration with the provided Issuer of "http://localhost:9000"` og `Connection refused`. Start authorization serveren igjen, og send 07: nå `401` med `no matching key(s) found`, fordi serveren har laget nye nøkler. Hent et nytt brukertoken: `200`, uten at API-et er startet på nytt.
**Viktig:** stopper du bare authorization serveren uten å starte API-et på nytt, gir 07 fortsatt `200` *(målt)*. Decoderen og nøklene er allerede hentet og ligger i minnet.
**Hvorfor:** deltakerne møter akkurat disse feilene i oppgave 6.3.

**Plan B:** slide 16 (discovery-dokumentet) og slide 14 for 500.

---

## D3. Scopes og roller (2,5 min)

**Mål:** vis 2x2-tabellen: scopet sier hva appen får gjøre, rollen sier hva brukeren får gjøre.

**Hva:** stopp «D2». Kjør **«D3»** (profil `d3`). Åpne `demo/D3SecurityConfig.java`. Pek på: `hasAuthority("SCOPE_companies.read")` for GET, `hasAuthority("SCOPE_companies.write")` for POST og DELETE, og converteren som slår sammen `SCOPE_...` og `ROLE_...`. Åpne `service/CompanyService.java`: `@PreAuthorize("hasRole('ADMIN')")` på `register` og `delete`.
**Forventet** *(målt, profil `d3`)*:

| Forespørsel | Status | Det du sier |
|---|---|---|
| 05 list as machine | 200 | «Maskinen har lese-scopet.» |
| 06 register as machine | 403 | «Ikke noe skrive-scope.» |
| 07 list as user | 200 | |
| 10 register as user (read, write) | 403 | «Riktig scope, feil rolle.» |
| 11 register as admin, read scope only | 403 | «Riktig rolle, feil scope. Appen fikk ikke lov til å skrive.» |
| 12 register as admin (read, write) | 201 | «Begge deler.» |
| 14 delete as admin (read, write) | 204 | |

`/api/me` for maskinen: `{"authorities":["FACTOR_BEARER","SCOPE_companies.read"],"name":"machine","type":"JwtAuthenticationToken"}`. For `admin` med bare lese-scopet: `["FACTOR_BEARER","ROLE_ADMIN","SCOPE_companies.read","SCOPE_openid"]` *(målt)*. Et ukjent selskap gir `404` (ikke 403: `/error` står i `permitAll()`, som i økt 4) *(målt)*.
Med reglene som bare ser på roller (profil `d2`) ble resultatet et annet *(målt)*: maskinen fikk `403` selv på GET, og `admin` med bare lese-scopet kunne registrere (`201`), fordi ingenting så på scopet.
**Hvorfor:** dette er oppgave 6.4, og grunnen til at 2x2-tabellen betyr noe: scope og rolle svarer på to ulike spørsmål.

**Plan B:** tabellen på slide 13.

---

## Etter demoene: tilbakestill

```
git checkout -- .
git clean -fd
git checkout session6-start
```

(Forkaster alle live-endringer i `demo`-branchen. Taggene røres ikke.) Stopp begge applikasjonene.

Til oppgavene bruker deltakerne `session6-start`. Til felles gjennomgang bruker du `session6-solution`.

---

## Felles gjennomgang (15 min, `session6-solution`)

```
git checkout session6-solution
```

1. **(4 min) Tabellen fra 6.4.** La deltakerne rope ut status for hver rad (maskin, bruker, admin med lesing, admin med begge): maskin GET `200` / POST `403`; bruker (read, write) POST og DELETE `403`; admin med bare lese-scope POST `403`; admin med begge POST `201` og DELETE `204` *(målt)*. Vis deretter koden i `tasks/SecurityConfig.java`: URL-regler på scopes, den sammenslåtte converteren og `@PreAuthorize` på `CompanyService`.
2. **(3 min) Det som forsvant.** Vis `git diff --stat session6-start session6-solution`: pakken `legacy` (fire klasser) er borte, hemmeligheten er borte, `application.yml` har `issuer-uri`, og API-et har 15 filtre som før. `POST /auth/login` gir nå `401` *(målt)*. *«Vi slettet innloggingen og fikk et bedre system.»*
3. **(3 min) Feilene.** Spør: *«Hvem fikk 500? Hvem fikk 401 med no matching key?»* Forklar den late decoderen, den gamle decoder-beanen og de nye nøklene ved hver omstart.
4. **(5 min) Spørsmål.** Typiske: *«Hvor lagrer klienten tokenet?»* (en single-page-app bør ikke ha det i `localStorage`; en backend for frontend holder det på serveren.) *«Hva med refresh tokens?»* (en offentlig klient som `postman` får ingen her; ID-porten gir et.) *«JWT eller opake tokens?»* (opake tokens sjekkes ved å kalle serveren, `oauth2ResourceServer(...opaqueToken())`; en JWT kan sjekkes uten å kalle serveren.) *«Hva er annerledes med ID-porten og Maskinporten?»* (andre adresser og scope-navn, registrerte klienter, og Maskinporten autentiserer klienten med en signert JWT; API-koden er den samme: `issuer-uri` og scopene.) *«Hvorfor sjekkes ikke audience?»* (den er valgfri i Springs standardoppsett. Fasiten har den: auth-serveren legger til `aud: company-api`, og API-et setter `audiences: company-api`; da gir `id_token` `401`, `The aud claim is not valid` *(målt)*. Ekstraoppgave 6.6 a.)

Bro til utsjekk: *«I dag tidlig skrev vi ut filterkjeden. Så la vi et token inn i den, og så lot vi noen andre lage tokenet. I hvert steg så kjeden lik ut: et filter leser legitimasjonen, og AuthorizationFilter bestemmer.»*

### Oppsummering av hele kurset (1-2 min)

Dette er kursets siste økt, så avslutt med de seks aha-øyeblikkene:

- *Økt 1: jeg kan åpne panseret selv, og det Spring gjør, er forståelig.*
- *Økt 2: en forespørsel er en kallstakk gjennom beans, og jeg kan lese den.*
- *Økt 3: konfigurasjon er en stack av kilder, og jeg kan se hvilken som vant.*
- *Økt 4: sikkerhet er en kjede av filtre jeg kan skrive ut, lese og endre.*
- *Økt 5: et token er et signert postkort: alle kan lese det, ingen kan endre det.*
- *Økt 6: API-et logger ingen inn. Det stoler på en utsteder.*

*«Samme verktøy hele veien: debugger, Actuator, `--debug`, TRACE-loggen, Postman og jwt.io. Spring er ikke magi. Du kan åpne panseret selv.»*

---

## Det jeg ikke har sett selv (sjekk mens du øver)

- Alt i IntelliJ-grensesnittet (run configurations med profiler).
- Postman: OAuth 2.0-fanen (begge grants), callbacken `https://oauth.pstmn.io/v1/callback` med *Authorize using browser*, knappen for å tømme cookies og importen av samlingen. Flytene er kjørt med curl og PKCE-scriptet, og samlingen med `newman` (17 forespørsler, 22 av 22 påstander grønne mot `session6-solution`).
- Innloggingssiden til authorization serveren i en nettleser (Spring Authorization Servers standardskjema).
- jwt.io i en nettleser.
- At Postmans token godtas når det sendes med *Use Token* fra OAuth 2.0-fanen; samlingen bruker sine egne `Bearer {{variabel}}`-headere.
- Fasitens ekstra oppførsel, som **ikke** er med i demoen: audience (`aud: company-api`) i auth-serveren og `audiences` i API-et (oppgave 6.6 a). Start- og demo-taggen har det ikke: der har maskintokenet `aud: machine` og blir godtatt.
- `SecurityRulesTest` i fasiten på Boot 4: trenger `spring-boot-starter-webmvc-test`, `spring-boot-starter-security-test` og den nye importen av `@AutoConfigureMockMvc`. Da er 12 av 14 grønne; de to converter-testene må ta høyde for `FACTOR_BEARER` *(målt)*.
- En ekte andre innlogging i nettleseren som `admin` etter `user`: med curl og cookie-jar svarer en ny authorize-forespørsel i samme sesjon `302` med en code, uten innlogging (single sign-on) *(målt)*.

---

## Slides og demoer: når du bytter

Decket er `session6-slides.pptx` (17 slides). Speaker notes på hver slide sier hva du skal si og når du bytter.

| Tid | Modus | Slides / demo | Min |
|---|---|---|---|
| 13:15 | Slides | 1 Tittel (oppsummering av økt 5), 2 Agenda, 3 Mål, 4 Oppsettsjekk, 5 Tre verktøy | 7 |
| 13:22 | Slides | 6 Hvem utsteder tokenet, 7 Fire roller i OAuth2 | 2,5 |
| | Slides | 8 Authorization Code med PKCE, 9 Client Credentials | 2,5 |
| | **Demo 1** (Postman, nettleser) | Tokens i Postman | 4 |
| | Slides | 10 Scope eller rolle, 11 OIDC, ID-porten og Maskinporten, 12 API-et trenger bare en adresse | 3 |
| | **Demo 2** (IntelliJ, Postman) | API-et som resource server | 4 |
| | Slides | 13 Scopes og roller blir authorities | 1 |
| | **Demo 3** (IntelliJ, Postman) | Scopes og roller | 2,5 |
| | Slides | 14 Feil du kommer til å møte, 15 Din tur | 1 |
| 13:45 | Oppgaver | deltakerne jobber (slide 15 blir stående) | 75 |
| 15:00 | Felles gjennomgang | `session6-solution` | 15 |
| 15:15 | Utsjekk | | |

Summen av minuttene over er 7 + 2,5 + 2,5 + 4 + 3 + 4 + 1 + 2,5 + 1 = 27,5, med rundt 2 minutter slakk.
Backup-slides: 16 (discovery-dokumentet, hvis nettleseren eller TRACE svikter i demo 2) og 17 (Authorization Code i curl, hvis Postman eller innloggingen i nettleseren svikter i demo 1).
