# Økt 6: oppgaver

I økt 5 laget API-et ditt tokenene selv: det logget brukeren inn og signerte tokenet med en delt hemmelighet. I dag lager ikke API-et tokens lenger. Noen andre logger brukeren inn og signerer tokenet, og API-et ditt sjekker det bare. Du skal se på denne andre parten (authorization serveren), hente tokens fra den og gjøre API-et om til en resource server for den. Hver oppgave har **Mål** (hva), **Hvorfor** (hva du lærer), **Slik gjør du det** og **Ferdig når**.

Dokumentet har to typer innhold:
- Vanlig tekst er oppgaveteksten deltakerne får.
- Bokser merket **🔎 For validering** er for deg som validerer oppsettet. De fjernes før oppgavene deles ut.

Verdier merket *(målt)* er målt på Spring Boot 4.1.2 (Spring Security 7) med JDK 25, med `curl`, `newman` og MockMvc. Punkter merket *(ikke sjekket)* er ikke kjørt.

---

## Før du begynner

1. Ha JDK 25, IntelliJ, Maven og Postman. Du trenger ikke dine egne svar fra økt 5: start-taggen har den ferdige koden fra økt 5.
2. Branchen har øktnummer i navnet, slik at den ikke kolliderer med branchene fra de andre øktene:

   ```
   git checkout -b my-answers-6 session6-start
   ```

3. Åpne mappen `session-6/` (mappen med `pom.xml`, ikke repo-roten) i IntelliJ. Maven-vinduet skal vise `auth-server` og `api`.
4. Start **authorization serveren** (`AuthServerApplication` i `auth-server`, port 9000) og **API-et** (`CompanyApplication` i `api`, port 8080).
5. Åpne <http://localhost:9000/.well-known/openid-configuration> i nettleseren. Du skal se JSON.
6. Importer `api/postman/session6.postman_collection.json` i Postman. Samlingen har variablene `baseUrl`, `authUrl`, `machineToken`, `userToken`, `adminReadToken`, `adminToken` og flere.
7. Send forespørsel **04 List companies, no token**. Du skal få `401`.

Personene og klientene du møter:

| Navn | Hva | Innlogging |
|---|---|---|
| `postman` | en klientapp for personer, uten hemmelighet | Authorization Code med PKCE. Scopes `openid`, `companies.read`, `companies.write` |
| `machine` | et annet system | Client Credentials, hemmelighet `machine-secret`, scope `companies.read` |
| `user` / `password` | en person med rollen `USER` | innloggingssiden til authorization serveren |
| `admin` / `admin` | en person med rollen `ADMIN` | innloggingssiden til authorization serveren |

Verktøyene du bruker:
- **Postman**: fanen *Authorization* med typen *OAuth 2.0* (*Get New Access Token*), og samlingen.
- **jwt.io**: lim inn et token for å lese det.
- **TRACE-loggen**: start API-et med profilen `trace` (*Run → Edit Configurations → Active profiles*: `trace`).

> **🔎 For validering** *(målt)*: begge applikasjonene starter. `GET /api/companies` uten token gir `401`, og `POST /auth/login` med `user` / `password` gir `200` (innloggingen fra økt 5 ligger fortsatt i API-et). Kodefasiten er `git diff session6-start session6-solution`.

---

## Oppgave 6.1: Se på authorization serveren

**Mål:** åpne disse i nettleseren, og svar på spørsmålene:
1. <http://localhost:9000/.well-known/openid-configuration> (*discovery-dokumentet*)
2. `jwks_uri` derfra (de *offentlige nøklene*)

**Hvorfor:** API-et ditt får ingen hemmelighet. Det får en adresse (`issuer`), og fra den adressen finner det alt det trenger for å sjekke et token.

**Spørsmål:**
- Hvilken URL er `issuer`? Hvilken er `token_endpoint`, og hvilken er `authorization_endpoint`?
- Hvilke grant types støtter serveren? Hvilken signeringsalgoritme bruker nøklene? Hva er `kid`?
- I økt 5 signerte API-et med en delt hemmelighet (HS256). Her er nøklene offentlige. Hvorfor holder en offentlig nøkkel for å *sjekke* et token, men ikke for å *lage* ett?
- Hva skjer med nøklene når authorization serveren starter på nytt? (Prøv, og hent `jwks_uri` igjen.)

**Ferdig når:** du kan si hvilke deler av discovery-dokumentet API-et ditt kommer til å bruke.

> **🔎 For validering** *(målt)*: `issuer` er `http://localhost:9000`, `token_endpoint` `.../oauth2/token`, `authorization_endpoint` `.../oauth2/authorize`, `jwks_uri` `.../oauth2/jwks`. `grant_types_supported` er `authorization_code`, `client_credentials`, `refresh_token` og `urn:ietf:params:oauth:grant-type:token-exchange`. `code_challenge_methods_supported`: `["S256"]`. JWKS har én RSA-nøkkel (`kty: RSA`, tokenene bruker `RS256`) med en `kid`. `scopes_supported` lister bare `openid`. Nøklene lages ved hver oppstart, så etter en omstart har `kid` en ny verdi.

---

## Oppgave 6.2: Hent tokens i Postman

**Mål:**
1. Hent et token for maskinen med **Client Credentials** i Postman (*Authorization → OAuth 2.0*): Access Token URL `http://localhost:9000/oauth2/token`, Client ID `machine`, Client Secret `machine-secret`, Scope `companies.read`, *Send as Basic Auth header*. (Eller kjør forespørsel **02** i samlingen.)
2. Hent et token for en person med **Authorization Code (With PKCE)**: Auth URL `http://localhost:9000/oauth2/authorize`, Access Token URL som over, Client ID `postman`, ingen hemmelighet, Callback URL `https://oauth.pstmn.io/v1/callback` (*Authorize using browser*), Code Challenge Method *SHA-256*, Scope `openid companies.read companies.write`. Logg inn som `user` / `password`. Gjør det samme igjen som `admin` / `admin`.
3. Lim hvert access token inn på jwt.io, og fyll ut tabellen:

   | Token | `sub` | `aud` | `scope` | `roles` | levetid |
   |---|---|---|---|---|---|
   | machine | | | | | |
   | user | | | | | |
   | admin | | | | | |

4. Hent også et `admin`-token med bare scopet `companies.read`. Lagre tokenene i samlingsvariablene `machineToken`, `userToken`, `adminToken` (lese og skrive) og `adminReadToken` (bare lese).

**Hvorfor:** dette er det en klientapp gjør mot ID-porten, Maskinporten eller en annen authorization server. Nå er du klienten.

**Spørsmål:**
- Hvem ser passordet til `user` i Authorization Code? Hvem ser det *ikke*?
- Hva er `code` som kommer tilbake i redirecten, og hva sender klienten sammen med den for å få tokenet? Hva mangler en tyv som stjeler `code`?
- Hvorfor har maskintokenet ingen `roles`?
- Den andre innloggingen som `admin` kan logge deg inn som `user` uten å spørre. Hvorfor? Hvordan får du en ren innlogging? (Postman har en knapp for å tømme cookies.)

**Ferdig når:** du har de fire tokenene i samlingsvariablene, og tabellen er fylt ut.

> **🔎 For validering** *(målt med curl og et PKCE-script, `verktoy/hent-token-pkce.py`; selve Postman-fanen er ikke sjekket)*: Maskin: `sub: machine`, `aud: machine`, `scope: ["companies.read"]`, ingen `roles`. Bruker: `sub: user`, `aud: postman`, `scope: ["companies.read","openid","companies.write"]`, `roles: ["ROLE_USER"]`. Levetid 30 minutter (`expires_in: 1799`; standard for serveren er 300 s). Svaret fra Authorization Code har også et `id_token` (`sub: user`, `aud: postman`, `azp`, `auth_time`, `sid`, uten scope og uten roller) og **ikke** noe `refresh_token` (serveren gir ikke refresh tokens til en offentlig klient uten hemmelighet). Ber maskinen om `companies.write`, svarer serveren `{"error":"invalid_scope"}`. Den stille andre innloggingen skyldes at nettleseren har en innloggingssesjon hos authorization serveren *(målt med curl og cookie-jar: en ny authorize-forespørsel i samme sesjon svarer `302` med en code, uten innloggingsside)*.

---

## Oppgave 6.3: Gjør API-et til en resource server for authorization serveren

**Mål:**
1. I `application.yml` i `api`: legg til `spring.security.oauth2.resourceserver.jwt.issuer-uri: http://localhost:9000`. Start API-et på nytt og send forespørsel **07** (brukertoken). Hva skjer, og hvorfor?
2. Slett pakken `no.course.company.legacy` (innloggingen og den delte hemmeligheten fra økt 5), og fjern `app.jwt` og `app.legacy` fra `application.yml`.
3. Fjern `/auth/login` fra `permitAll()` i `SecurityConfig`.
4. Start API-et på nytt. Kall `GET /api/companies` med `userToken` og med `machineToken` (forespørslene **07** og **05**).

**Hvorfor:** API-et vet ikke lenger hvordan det skal signere. Det spør utstederen om de offentlige nøklene og sjekker signatur, utløp og `iss`. Du sletter kode, og sikkerheten blir bedre.

**Slik gjør du det:** du skriver ingen `JwtDecoder`. Spring Boot lager en fra `issuer-uri`, så lenge det ikke finnes en `JwtDecoder`-bean fra før.

**Spørsmål:**
- Hva returnerer 07 og 05, og hvorfor er de ulike?
- Hva sier loggen (profil `trace`) om authorities for maskintokenet? (Sammenlign med oppgave 5.3.)
- Stopp authorization serveren, og start API-et på nytt. Starter API-et? Hva returnerer det første kallet med et token? Start authorization serveren igjen. Virker det gamle tokenet? Må du starte API-et på nytt?
- Hvem sjekker at tokenet kommer fra *denne* utstederen?

**Ferdig når:** `07` gir `200`, maskinkallet har en status du kan forklare, og du har prøvd begge feilsituasjonene.

> **🔎 For validering** *(målt)*:
> - Med `issuer-uri`, men *før* `legacy` er slettet: `401` med `error_description="An error occurred while attempting to decode the Jwt: Signed JWT rejected: Another algorithm expected, or no matching key(s) found"`. Den gamle `JwtDecoder`-beanen (HS256) vinner, fordi Spring Boot bare lager sin egen decoder når det ikke finnes noen. På Boot 4 har headeren i tillegg `error_uri` og `resource_metadata`.
> - Etter rettingen: bruker `200`; maskin `403`. TRACE: `Looking for scopes in claim roles`, og `Granted Authorities=[FactorGrantedAuthority [authority=FACTOR_BEARER, …]]`, altså ingen `ROLE_`- eller `SCOPE_`-authority. (`FACTOR_BEARER` legges til av Spring Security 7 og sier bare at brukeren kom inn med et token.) `POST /auth/login`: `404` så lenge den står i `permitAll()` (controlleren er borte), `401` når den er fjernet (`denyAll()` fanger den).
> - Authorization serveren nede når API-et starter: API-et starter. Det første kallet med et token gir **500**, og loggen har `JwtDecoderInitializationException: Failed to lazily resolve the supplied JwtDecoder instance`, `Unable to resolve the Configuration with the provided Issuer of "http://localhost:9000"` og `Connection refused`. Når serveren er oppe igjen, har den nye nøkler: det gamle tokenet gir `401` (`no matching key(s) found`), og et nytt token gir `200` uten at API-et startes på nytt. Merk: stopper du serveren *etter* at API-et har gjort ett kall, gir neste kall fortsatt `200`, fordi decoderen og nøklene allerede er hentet.
> - Utstederen: `JwtIssuerValidator` er med i standardvalidatorene når `issuer-uri` brukes *(en ren `iss`-feil er ikke kjørt: et token fra en annen server på en annen port feiler tidligere, med `no matching key(s) found`, fordi nøklene er ulike; målt)*.
> - Audience sjekkes ikke: maskintokenet (`aud: machine`) godtas av API-et (`200` når scopet er riktig).

---

## Oppgave 6.4: Scopes og roller

**Mål:** maskinen skal kunne lese selskaper, men ikke skrive. En person får bare skrive hvis appen har fått lov til å skrive *og* personen er admin.
1. La URL-reglene sjekke scopes: `GET` krever `SCOPE_companies.read`, `POST` og `DELETE` krever `SCOPE_companies.write`.
2. La `JwtAuthenticationConverter` gi både scopene (`SCOPE_...`, standarden) og rollene (`ROLE_...`, fra claimet `roles`) som authorities.
3. Behold `@PreAuthorize("hasRole('ADMIN')")` på `register` og `delete` i `CompanyService`.
4. Send forespørslene **05–14** i samlingen (alle består når du er ferdig).

**Hvorfor:** et *scope* sier hva *klientappen* får gjøre på vegne av brukeren. En *rolle* sier hva *brukeren* får gjøre. Ekte API-er trenger ofte begge.

**Slik gjør du det:**
- `hasAuthority("SCOPE_companies.read")`
- To `JwtGrantedAuthoritiesConverter`: én med standardinnstillingene, og én med `setAuthoritiesClaimName("roles")` og `setAuthorityPrefix("")`. I `converter.setJwtGrantedAuthoritiesConverter(jwt -> ...)` legger du begge resultatene i én liste.
- Kall `/api/me` (forespørsel 08 og 09) for å se authorities. Du ser også `FACTOR_BEARER`: Spring Security 7 legger den til for alle som logger inn med et token. Den er verken et scope eller en rolle.

**Fyll ut tabellen ved å prøve** (hva får du, og hvorfor?):

| Token | GET | POST | DELETE |
|---|---|---|---|
| ingen token | | | |
| machine (read) | | | |
| user (read, write) | | | |
| admin (bare read) | | | |
| admin (read, write) | | | |

**Spørsmål:**
- Hvorfor kan `user` med skrive-scopet likevel ikke skrive? Hvorfor kan `admin` med bare lese-scopet ikke skrive?
- Hvor sjekkes scopet, og hvor sjekkes rollen? Hvilket filter gjør begge avslagene om til `403`?
- Hva ville gått galt hvis du bare sjekket rollen, som i økt 5?

**Ferdig når:** forespørslene 05–14 består, og tabellen er fylt ut.

> **🔎 For validering** *(målt i `session6-demo-done`, profil `d3`)*:
>
> | Token | GET | POST | DELETE |
> |---|---|---|---|
> | ingen token | 401 | | |
> | machine (read) | 200 | 403 | 403 |
> | user (read, write) | 200 | 403 | 403 |
> | admin (bare read) | 200 | 403 | 403 |
> | admin (read, write) | 200 | 201 | 204 |
>
> `/api/me` for maskinen: `{"authorities":["FACTOR_BEARER","SCOPE_companies.read"],"name":"machine","type":"JwtAuthenticationToken"}`. Admin med lese-scope: `["FACTOR_BEARER","ROLE_ADMIN","SCOPE_companies.read","SCOPE_openid"]`. Scopet sjekkes i URL-regelen (`AuthorizationFilter`), rollen i `@PreAuthorize` på servicen. Begge gir `403` via `ExceptionTranslationFilter`. Med bare roller (reglene fra økt 5, profil `d2`) får maskinen `403` selv på GET, og `admin` med lese-scope kan registrere (`201`). Et ukjent selskap gir `404` *(målt)*.
>
> Fasit: `tasks/SecurityConfig.java` i `session6-solution`.

---

## Oppgave 6.5: Test reglene uten server

**Mål:** i `SecurityRulesTest`, test radene i tabellen fra 6.4 med MockMvc og `jwt()`-postprosessoren, uten å starte authorization serveren.

**Hvorfor:** en test som trenger en kjørende authorization server, er treg og skjør. Med `jwt()` bestemmer du selv hva tokenet inneholder.

**Slik gjør du det:**
- `@SpringBootTest` og `@AutoConfigureMockMvc`. På Spring Boot 4 ligger `@AutoConfigureMockMvc` i `org.springframework.boot.webmvc.test.autoconfigure`. Hvis `api/pom.xml` ikke har `spring-boot-starter-webmvc-test` og `spring-boot-starter-security-test` (begge med `<scope>test</scope>`), legg dem til.
- `get("/api/companies").with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_companies.read")))`
- Minst: lesing med lese-scopet gir `200`; ingen authority gir `403`; ingen token i det hele tatt gir `401`; skriving med skrive-scopet, men uten `ROLE_ADMIN`, gir `403`.

**Spørsmål:**
- `jwt()` kjører verken `JwtDecoder` eller converteren din. Hva betyr det for hva testen beviser?
- Hvilken test ville du skrevet for å sjekke converteren, og hvordan?

**Ferdig når:** testene dine er grønne.

> **🔎 For validering** *(målt i `session6-solution`)*:
> - Testene kjører **uten authorization server**: `JwtDecoder` fra `issuer-uri` lages først når den trengs, så konteksten laster.
> - På Boot 4 trenger fasiten `spring-boot-starter-webmvc-test`, `spring-boot-starter-security-test` (i stedet for `spring-security-test`) og den nye importen av `@AutoConfigureMockMvc`. Da er 12 av 14 tester grønne. De to som tester converteren direkte, forventer bare scopes og roller, men får også `FACTOR_BEARER`: maskintoken `[SCOPE_companies.read, FACTOR_BEARER]`, admin `[SCOPE_companies.read, SCOPE_openid, ROLE_ADMIN, FACTOR_BEARER]`. Fasiten må oppdateres.
> - `jwt()` med `SCOPE_companies.read`: GET `200`. `jwt()` uten authority: `403`. `jwt()` med bare `ROLE_ADMIN`: GET `403`. Maskin (read) POST `403`. Bruker (read, write, `ROLE_USER`) POST og DELETE `403`. Admin med lese-scope POST `403`. Admin med lese- og skrive-scope: POST `201`, DELETE `204`. Ingen token: `401`.
> - `jwt()` bruker ikke decoderen og ikke vår converter: en Jwt med claimet `roles: ["ROLE_ADMIN"]`, men uten authorityen `ROLE_ADMIN`, får `403` (en test viser det). For å teste converteren kaller du `converter.convert(jwt)` på en bygget `Jwt`.

---

## Oppgave 6.6: Ekstra (valgfritt)

**Mål:** velg én eller flere:
- (a) **Audience.** Maskintokenet godtas av API-et, selv om det er ment for `machine`. La authorization serveren legge `company-api` i `aud` (en token customizer, eller `aud` per klient), og la API-et avvise tokens uten den (`JwtClaimValidator` i en `DelegatingOAuth2TokenValidator`).
- (b) **ID-tokenet.** Svaret fra Authorization Code har et `id_token` i tillegg til `access_token`. Dekod begge. Hvilket bør API-et godta, og hvorfor? Send `id_token` som Bearer-token til API-et. Hva skjer?
- (c) **Omstart.** Start authorization serveren på nytt, og kall API-et med et gammelt token. Hva får du, og hva gjør API-et med nøklene?
- (d) **Uten PKCE.** Prøv authorize-forespørselen uten `code_challenge`. Hva sier serveren, og hvorfor?

> **🔎 For validering:**
> - (a) *(målt)* implementert i `session6-solution`: customizeren i `AuthServerApplication` legger `aud: ["company-api"]` i access tokens, og `spring.security.oauth2.resourceserver.jwt.audiences: company-api` i API-et gjør resten (ingen ekstra kode). `id_token` som Bearer: `401`, `The aud claim is not valid`. Maskin- og brukertokens (nå med `aud: company-api`) godtas.
> - (b) *(målt)* `id_token` er en JWT signert med samme nøkkel, med `sub`, `aud: postman`, `azp`, `auth_time` og `sid`, men uten scope og roller. Sendt til API-et før (a): `403` (gyldig token, ingen scope). Etter (a): `401`.
> - (c) *(målt)* etter en omstart har JWKS en ny `kid`, og et token fra før omstarten gir `401` med `Signed JWT rejected: Another algorithm expected, or no matching key(s) found`. Et nytt token virker uten at API-et startes på nytt. Dette gjelder hvis API-et ikke har hentet nøklene før omstarten. Har det allerede validert et token, ligger de gamle nøklene i cachen en stund, og det gamle tokenet gir fortsatt `200`. Et token fra en *annen* authorization server (andre nøkler og annen `iss`) gir også `no matching key(s) found`, fordi signaturen sjekkes før utstederen *(målt)*.
> - (d) *(målt)* en authorize-forespørsel uten `code_challenge` blir sendt til callbacken med `error=invalid_request` og `error_description=OAuth 2.0 Parameter: code_challenge` (klienten har `require-proof-key: true`).

---

## 🔎 Oppsummering: hva som ikke er sjekket

- Postman-appen: OAuth 2.0-fanen (begge grants), callbacken `https://oauth.pstmn.io/v1/callback`, knappen for å tømme cookies og importen. Samlingen er kjørt med `newman` mot `session6-solution` på 4.1.2, med brukertokenene hentet av `verktoy/hent-token-pkce.py`: 17 forespørsler, 22 av 22 påstander grønne. `tamperedToken`-scriptet kjørte i `newman`.
- jwt.io i en nettleser.
- Innloggingssiden til authorization serveren i en nettleser (flyten er kjørt med et script som fyller ut skjemaet).
- IntelliJ-grensesnittet.
- Oppgave 6.6 (a) endrer `auth-server`, som ellers er «gitt». Fasiten har endringen.
