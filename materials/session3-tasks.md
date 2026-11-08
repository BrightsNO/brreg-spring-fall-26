# Økt 3: oppgaver

Du skal finne ut hvor en verdi kommer fra, flytte de faste verdiene ut av koden, og kjøre den samme applikasjonen i tre miljøer. Hver oppgave har **Mål** (hva), **Hvorfor** (hva du lærer), **Slik gjør du det** og **Ferdig når**.

Dokumentet har to typer innhold:
- Vanlig tekst er oppgaveteksten deltakerne får.
- Bokser merket **🔎 For validering** er for deg som validerer oppsettet. De fjernes før oppgavene deles ut.

Oppgave 3.6 er for dem som blir ferdige tidlig.

---

## Før du begynner

1. Klon repoet og sjekk ut `session3-start`:

   ```
   git checkout -b my-answers session3-start
   ```
2. Start `CompanyApplication`. Åpne <http://localhost:8080/actuator/health>. Du skal se `{"status":"UP"}`.
3. Importer filene i `postman/` til Postman (samlingen «Spring Boot course: session 3» og miljøet «Brights local»).

Verktøyene du bruker:
- **Actuator**: <http://localhost:8080/actuator/env> og <http://localhost:8080/actuator/configprops>
- **Oppstartsloggen**: linjen `The following 1 profile is active: "..."` og linjene om profilen.
- **Kjørekonfigurasjon**: *Active profiles*, *Environment variables* og *Program arguments* i IntelliJ.

Applikasjonen har en grense: registeret tar imot høyst 3 selskaper, og den fjerde POST-en gir `409`. Grensen, navnet på registeret og e-postadressen står skrevet i koden. Det er dette du endrer.

> **🔎 For validering:** ⚠️ *8. nov:* taggene `session3-start` og `session3-solution` **finnes ikke** (repoet har ingen tagger), så `git checkout -b my-answers session3-start` feiler med `fatal: 'session3-start' is not a commit`. Startkoden ligger på branchen `chore/spring-boot-4.1` (Boot 4.1.2), og der er `mvn verify` grønn med 7 tester *(målt)*. Min løsning av alle oppgavene ligger i `dag1-verifisering/okt3-mine-losninger.patch` og kan brukes som `session3-solution`.
>
> `session3-start` har 7 passerende tester *(målt)*: CRUD, validering, og grensen på 3 selskaper (`409` med `The register is full: at most 3 companies`). Kodefasiten er `git diff session3-start session3-solution`. Testprofil: grensetesten i fasiten bruker `@ActiveProfiles("test")` (grense 3) fordi standardprofilen `local` har grense 5.

---

## Oppgave 3.1: Hvor kommer denne verdien fra?

**Mål:** svar på disse spørsmålene om egenskapen `spring.application.name`:
1. Hvilken kilde gir verdien i dag, og i hvilken fil og linje?
2. Sett miljøvariabelen `SPRING_APPLICATION_NAME=from-env` i kjørekonfigurasjonen din. Hvilken kilde vinner nå? Står filen fortsatt oppført?
3. Legg også til programargumentet `--spring.application.name=from-args`. Hvilken kilde vinner nå?
4. Hva er rekkefølgen på **alle** kildene, fra først til sist?

**Hvorfor:** når en verdi er feil i produksjon, er «hvor kommer den fra?» det første spørsmålet. Du kan alltid spørre den kjørende applikasjonen.

**Slik gjør du det:** åpne <http://localhost:8080/actuator/env/spring.application.name> (eller Postman-forespørselen «One property»). Les `property.source` og listen `propertySources`. Hver oppføring med en verdi har en `origin`.

**Ferdig når:** du kan si, for hver av de tre situasjonene, hvilken kilde som vant og hvorfor.

> **🔎 For validering** *(målt på en ekte kjørende server, `session3-solution`)*: med bare filen er `source` `Config resource 'class path resource [application.yml]'` med verdien `company-session3`. Med `SPRING_APPLICATION_NAME=from-env` satt er `source` `systemEnvironment` med verdien `from-env`. Med argumentet også satt vinner `commandLineArgs` (`from-args`), og de andre kildene står fortsatt oppført under den *(målt for `registry.max-companies` og `demo.greeting` med samme rekkefølge)*. Rekkefølgen, fra først til sist: `server.ports` (Boot 4, uten verdi for denne egenskapen), `commandLineArgs`, `servletConfigInitParams`, `servletContextInitParams`, `systemProperties`, `systemEnvironment`, `random`, profilfilene, `application.yml`, `applicationInfo`, `Management Server`.

---

## Oppgave 3.2: Gjør verdiene konfigurerbare

**Mål:** navnet på registeret, grensen og avsenderadressen er skrevet i koden (`TODO 3.2`). Flytt dem til konfigurasjon, som et typet og validert objekt:

| Egenskap | Type | Regel | Standard i `application.yml` |
|---|---|---|---|
| `registry.name` | tekst | ikke tom | `Brights Company Register` |
| `registry.max-companies` | tall | 1 til 10000 | `100` |
| `registry.notifications.sender-address` | tekst | ikke tom | `noreply@company.example` |

**Hvorfor:** den samme jar-filen kjører overalt, og verdiene endres per miljø. Et typet objekt med validering stopper oppstarten hvis en verdi er feil.

**Slik gjør du det:**
1. `RegistryProperties` (i `tasks`) er en tom record. Gjør den til en `@ConfigurationProperties(prefix = "registry")` med `@Validated`, legg til de tre verdiene (avsenderadressen ligger i den nøstede recorden `notifications`) og valideringsannotasjonene.
2. Sørg for at Spring finner den: `@ConfigurationPropertiesScan` på `CompanyApplication`.
3. Legg verdiene til i `application.yml`.
4. Injiser `RegistryProperties` i `CompanyService` (grense og navn) og `EmailNotificationSender` (avsenderadresse), og fjern konstantene.
5. Test: start med programargumentet `--registry.max-companies=2` og POST tre selskaper (mappen «The register limit» i samlingen, med samlingsvariabelen `limit` satt til `2`). Den tredje gir `409`. Start deretter med `--registry.max-companies=0` og les meldingen.

   Registeret ligger i minnet, så start appen på nytt før du kjører mappen en gang til.

   Testen `CompanyLimitTest` venter grensen 3. Den blir rød nå som standarden er 100. Det fikser du i 3.3: gi testklassen `@ActiveProfiles("test")`.

**Ferdig når:** grensen følger egenskapen, og `--registry.max-companies=0` stopper applikasjonen med en melding som nevner egenskapen.

> **🔎 For validering** *(målt på en ekte server)*: med `--registry.max-companies=2` gir den tredje POST-en `409` med `"detail":"The register is full: at most 2 companies"`. Med `--registry.max-companies=0` er rapporten `APPLICATION FAILED TO START`, `Binding to target no.course.company.tasks.RegistryProperties failed:`, `Property: registry.maxCompanies` (navnet slik recorden skriver det), `Value: "0"`, `Origin: "registry.max-companies" from property source "commandLineArgs"`, `Reason: must be greater than or equal to 1`. Et tomt navn (`--registry.name=`) gir `Property: registry.name`, `Value: ""`, `Origin: "registry.name" from property source "commandLineArgs"`, `Reason: must not be blank`. `GET /actuator/configprops/registry` viser `properties` og, for hver verdi, en `inputs`-oppføring med `origin` (for eksempel `class path resource [application.yml] - 7:9`, eller `"registry.max-companies" from property source "commandLineArgs"`). Kodefasiten: `RegistryProperties`, `CompanyService` og `EmailNotificationSender` på `session3-solution`. I fasiten er `api-key` også en del av den nøstede recorden; det er oppgave 3.5.

---

## Oppgave 3.3: Tre profiler

**Mål:** lag tre profilfiler og kjør applikasjonen i hver av profilene `local`, `test` og `prod`:

| | `local` | `test` | `prod` |
|---|---|---|---|
| `registry.max-companies` | 5 | 3 | 10000 |
| `registry.name` | (standarden) | `Brights Company Register (test)` | (standarden) |
| actuator viser reelle verdier (`show-values`) | ja | ja | nei |
| actuator-endepunkter eksponert | det samme som nå | det samme som nå | bare `health` |
| loggnivå for `no.course.company` | `DEBUG` | (standarden) | (standarden) |

Standardprofilen må være `local`, slik at `mvn spring-boot:run` uten argumenter gir utvikleroppsettet.

**Hvorfor:** én kodebase, tre miljøer. En profilfil legger til i `application.yml` og vinner over den.

**Slik gjør du det:**
1. Lag `application-local.yml`, `application-test.yml` og `application-prod.yml` i `src/main/resources`. Flytt de to `show-values: ALWAYS`-linjene fra `application.yml` til profilfilene som trenger dem (`TODO 3.3`). Tabellen gjelder: både `local` og `test` skal vise verdiene, selv om kommentaren i filen bare nevner `local`.
2. Legg til `spring.profiles.default: local` i `application.yml`.
3. Kjør med *Active profiles* `test`, `prod`, og uten profil. Les linjen `The following 1 profile is active: "..."` (eller `No active profile set, falling back to 1 default profile: "local"`).
4. For hver kjøring, åpne `/actuator/env/registry.max-companies`. I `prod` gir den `404`, og det er poenget. Der sjekker du grensen ved å lese linjen `The following 1 profile is active: "prod"` og stole på filen, eller ved å starte én gang med `--management.endpoints.web.exposure.include=health,env` i tillegg.

**Ferdig når:** `local` og `test` viser grensen i tabellen og reelle verdier i `/actuator/env`, og `prod` gir `404` for `/actuator/env`.

> **🔎 For validering** *(målt, `ConfigurationBindingTest` på `session3-solution`)*: standardprofilen er `local` (grense 5); `test` gir grense 3 og et navn som slutter på `(test)`; `prod` gir grense 10000. Logglinjene er `No active profile set, falling back to 1 default profile: "local"` og `The following 1 profile is active: "test"` *(målt)*. Med `prod` og en nøkkel gir en kjørende `session3-solution` `404` for `/actuator/env`, `/actuator/configprops`, `/actuator/configprops/registry` og `/actuator/beans`, og `/actuator/health` gir `{"status":"UP"}` *(målt)*. Merk: dette gjelder bare fasiten. På `session3-demo-done` eksponerer `prod` fortsatt `/actuator/env` (`200`), fordi den grenens `application.yml` eksponerer det for alle profiler.

---

## Oppgave 3.4: Velg avsender med en profil

**Mål:** `CompanyService` har `@Qualifier("emailNotificationSender")` (`TODO 3.4`). Fjern den, og la profilen velge: `prod` bruker e-postavsenderen, alle andre profiler bruker SMS-avsenderen.

**Hvorfor:** valget mellom to beans er en egenskap ved miljøet, ikke ved koden som bruker beanen. Dette er `NoUniqueBeanDefinitionException` fra sesjon 1, løst på en tredje måte.

**Slik gjør du det:**
1. Fjern `@Qualifier` og importen fra konstruktøren i `CompanyService`.
2. Legg til `@Profile("prod")` på `EmailNotificationSender` og `@Profile("!prod")` på `SmsNotificationSender`.
3. Kjør med `local` og med `prod`. Registrer et selskap (POST) og les loggen.
4. Tenk: hva skjer hvis du kjører med en profil der **ingen** av avsenderne er aktiv? (Prøv: endre `@Profile("!prod")` til `@Profile("local")` midlertidig, kjør med `test`, les feilen, og sett den tilbake.)

**Ferdig når:** med `local` sier loggen `SMS: [Brights Company Register] Registered company: ...`, og med `prod` sier den `Email from noreply@company.example: ...`.

> **🔎 For validering** *(målt på en ekte server)*: med `test` er loggelinjen `SMS: [Brights Company Register (test)] Registered company: C1`. Med `prod` og en nøkkel er den `Email from noreply@company.example: [Brights Company Register] Registered company: Company 1`. Med ingen av avsenderne aktiv (`@Profile("local")` på SMS-avsenderen, kjørt med `test`) stopper applikasjonen ved oppstart med `Parameter 0 of constructor in no.course.company.service.CompanyService required a bean of type 'no.course.company.bean.NotificationSender' that could not be found.` *(målt)*.

---

## Oppgave 3.5: Hemmeligheter

**Mål:** e-postleverandøren trenger en API-nøkkel. Den må **aldri** være i repoet.
1. Legg til `apiKey` (ikke tom) i den nøstede recorden `notifications` i `RegistryProperties`.
2. `local` og `test` kan ha en harmløs dummy-nøkkel i sine profilfiler. `prod` må lese nøkkelen fra miljøvariabelen `NOTIFICATION_API_KEY`: `api-key: ${NOTIFICATION_API_KEY}` i `application-prod.yml`.
3. Start med `prod` og **uten** miljøvariabelen. Hva skjer? (Les nøye, og fiks deretter så applikasjonen ikke starter.)
4. Start med `prod` og `NOTIFICATION_API_KEY` satt til en testverdi du velger selv i kjørekonfigurasjonen. Den må starte.
5. Legg `secrets.properties` og `*-secrets.yml` til i `.gitignore`.

**Hvorfor:** en hemmelighet i repoet er en lekket hemmelighet. Miljøet (eller en hemmelighetslagring) er stedet for den. Og en manglende hemmelighet må stoppe applikasjonen, ikke erstattes av en feil verdi.

**Slik gjør du det:** steg 3 har en felle. Prøv `${NOTIFICATION_API_KEY}` først og sjekk loggen for nøkkelen, og les deretter om standardverdi-syntaksen `${NAME:default}`. En standardverdi som er tom, sammen med `@NotBlank`, gir resultatet du vil ha.

**Ferdig når:** `prod` uten variabelen feiler med `Property: registry.notifications.apiKey` og `Reason: must not be blank`; med variabelen starter den; ingen nøkkelverdi står skrevet noe sted i repoet (`git grep` etter testverdien din finner ingenting).

> **🔎 For validering** *(målt på en ekte server)*: med `${NOTIFICATION_API_KEY}` og **uten** standardverdi **starter** `--spring.profiles.active=prod`, og nøkkelen er den bokstavelige teksten `${NOTIFICATION_API_KEY}`. Med `${NOTIFICATION_API_KEY:}` feiler den med `Property: registry.notifications.apiKey`, `Value: ""`, `Reason: must not be blank`. Med `NOTIFICATION_API_KEY` satt til en hvilken som helst verdi som ikke er tom, starter den og bruker e-postavsenderen. `ConfigurationBindingTest` dekker feilen og suksessen med `--registry.notifications.api-key=...`. Maskerte verdier i `/actuator/env` har formen `"value":"******"`, med `source` og `origin` fortsatt synlige *(målt med `show-values=NEVER` på en ekte server)*. Om `prod` eksponerer endepunktet, er oppgave 3.3.

---

## Oppgave 3.6: Stretch, navnet på en miljøvariabel (valgfritt)

**Mål:** hvilke skrivemåter av en miljøvariabel setter `registry.max-companies`? Prøv `REGISTRY_MAX_COMPANIES` og `REGISTRY_MAXCOMPANIES`. Sammenlign deretter `/actuator/env/registry.max-companies` med `/actuator/configprops/registry` i hvert tilfelle.

**Hvorfor:** relaxed binding. Og en påminnelse om å lese den typede visningen når den rå visningen ser feil ut.

> **🔎 For validering** *(målt på en ekte server, `local`-profilen)*: begge skrivemåtene binder. `configprops` viser 9 og 8, med `origin` `System Environment Property "REGISTRY_MAX_COMPANIES"` / `"REGISTRY_MAXCOMPANIES"`. `/actuator/env/registry.max-companies` viser den nye verdien bare for `REGISTRY_MAX_COMPANIES` (source `systemEnvironment`, value `9`); for `REGISTRY_MAXCOMPANIES` viser den fortsatt verdien fra filen (5).

---

## 🔎 Oppsummering: hva som ikke er sjekket

*8. nov:* alle oppgavene 3.1–3.6 er løst og målt på nytt på Boot 4.1.2 (Temurin JDK 25.0.5, `java -jar`, `curl` og `newman`). Verdiene i boksene over stemmer, med unntakene som er merket *8. nov*.

- Alt i IntelliJ-grensesnittet (kjørekonfigurasjoner med profiler, variabler og argumenter). Alle kjøringer ble startet fra shellen med `java -jar` og `env`.
- Postman-appens grensesnitt: samlingen ble kjørt med `newman`, ikke klikket i appen.
- Windows.
