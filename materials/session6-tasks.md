# Økt 6: oppgaver

I økt 5 laget API-et ditt tokenene selv: det logget brukeren inn og signerte tokenet med en delt hemmelighet. I dag lager ikke API-et tokens lenger. Noen andre logger brukeren inn og signerer tokenet, og API-et ditt sjekker det bare. Du skal se på denne andre parten (authorization serveren), hente tokens fra den og gjøre API-et om til en resource server for den. Hver oppgave har **Mål** (hva), **Hvorfor** (hva du lærer), **Slik gjør du det** og **Ferdig når**.

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

---

## Oppgave 6.6: Ekstra (valgfritt)

**Mål:** velg én eller flere:
- (a) **Audience.** Maskintokenet godtas av API-et, selv om det er ment for `machine`. La authorization serveren legge `company-api` i `aud` (en token customizer, eller `aud` per klient), og la API-et avvise tokens uten den (`JwtClaimValidator` i en `DelegatingOAuth2TokenValidator`).
- (b) **ID-tokenet.** Svaret fra Authorization Code har et `id_token` i tillegg til `access_token`. Dekod begge. Hvilket bør API-et godta, og hvorfor? Send `id_token` som Bearer-token til API-et. Hva skjer?
- (c) **Omstart.** Start authorization serveren på nytt, og kall API-et med et gammelt token. Hva får du, og hva gjør API-et med nøklene?
- (d) **Uten PKCE.** Prøv authorize-forespørselen uten `code_challenge`. Hva sier serveren, og hvorfor?
