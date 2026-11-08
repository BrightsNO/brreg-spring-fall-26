# Session 3: forelesers manus (steg, hva og hvorfor)

Session 3 går fra 13:20 til 15:20 (30 min teori og demoer, deretter oppgaver og 15 min felles gjennomgang). Det er **ingen innsjekk**: de første minuttene er en repetisjon av session 2 og en rask sjekk av oppsettet. Dagsplanen finnes i `materials/day1-schedule.md`. Utsjekk følger kl. 15:20 (10 min).

For hvert steg får du **hva** du gjør, **hva skjer**, **forventet** output og **hvorfor** (budskapet til deltakerne).

Verdier merket *(measured)* ble observert da prosjektet ble kjørt på Spring Boot 4.1.2 (Temurin 25.0.5, Maven). Verdiene i dette manuset er målt på en **kjørende applikasjon** (`java -jar` med programargumenter og miljøvariabler, `curl` mot `/actuator/*`, og `newman` for Postman-samlingen), med mindre de er merket som test. Punkter merket *(not checked)* har jeg ikke fått kjørt: IntelliJ-kjøreprofilene og Windows-syntaksen.

**Obs, per 8. nov:** repoet `session-3` har bare start-koden (ingen tagger, ingen `demo/`-pakke, ingen `application-{profil}.yml`). D1–D4 er derfor målt på en gjenskapt demovariant (samme klasser og filer som beskrevet i dette manuset: `DemoSettings`, `Greeter`-bønnene, `ConfigPrinter`, `BrokenConfiguration` og profilfilene `local`/`test`/`prod`) bygget på start-koden. Verdiene i «Felles gjennomgang» (oppgave 3.2–3.6) og testtallene for `session3-demo-done` og `session3-solution` er sist målt på 3.5.16 og er ikke målt på nytt, fordi løsningskoden ikke finnes i repoet.

---

## Den røde tråden

Session 1 og 2 viste at bønner lages fra klasser, og at en forespørsel er en kallstack gjennom bønner. Session 3 viser hvor **verdiene** kommer fra: konfigurasjon er en stack av kilder, den første med navnet vinner, og Spring kan fortelle deg hvilken som vant.

Aha-øyeblikket: *"Konfigurasjon er en stack av kilder, og jeg kan se hvilken som vant."* Samme idé som hele dagen: Spring er ikke magi, jeg kan åpne panseret selv.

Fire demoer bærer sesjonen:

1. D1: hvor kommer en verdi fra? `/actuator/env` og rekkefølgen på kildene.
2. D2: typede innstillinger med `@ConfigurationProperties`, og en ugyldig verdi som stopper applikasjonen med en lesbar melding.
3. D3: den samme applikasjonen med tre profiler, og bønner som bare finnes i én profil.
4. D4: hemmeligheter: maskerte verdier i actuatoren, og en manglende placeholder.

Demoprosjektet (`session3-demo-done`) bruker et eget prefiks `demo.*` og enkle bønner (`Greeter`), så det avslører ikke oppgavene (`registry.*`-innstillingene, senderne).

---

## Før du starter (dagen før, og 10 min før)

1. Åpne `session-3` i IntelliJ. Maven-synk, SDK 25.
2. Opprett en gren:

   ```
   git checkout -b demo session3-demo-done
   ```
3. Opprett fire kjøreprofiler for `CompanyApplication`:
   - **default**: ingen profil (standardprofilen er `local`).
   - **test**: *Active profiles* `test`.
   - **prod**: *Active profiles* `prod`.
   - **broken**: *Active profiles* `broken,local`.
   Hver har feltene *Environment variables* og *Program arguments* som du bruker i D1.
4. Importer `postman/session3.postman_collection.json` og miljøet i Postman.
5. Kjør alle fire demoene én gang. Ta skjermbilder som plan B: svaret fra `/actuator/env/demo.greeting` og rapporten `APPLICATION FAILED TO START`.
6. Port 8080 ledig. Stor IDE-skrift. Tøm konsollen før hver demo.
7. `mvn verify` er grønn på alle tre taggene *(measured)*, se «Målte resultater» til slutt.

---

## Åpning (slides 1-4, 4 min)

- Slide 1 og 2: 1 min sammen. Agendaen viser hele dagen.
- Slide 3 (mål): repetisjon av session 2 på to setninger: *"En forespørsel er en kallstack gjennom bønner. Kontrolleren, filtrene og advicet er bønner."* Deretter de tre målene.
- Slide 4 (oppsettsjekk): `git checkout -b my-answers session3-start`, start applikasjonen, `/actuator/health`, importer samlingen, og kjør «Health» i Postman. De som står fast, jobber videre mens du fortsetter.

---

## D1. Hvor kommer denne verdien fra? (6 min)

**Mål:** den samme egenskapen kan settes på fem steder, og `/actuator/env` viser hvilken som vant.

Filer: `application.yml` (`demo.greeting: Hello from application.yml`), `application-local.yml` (`Hello from application-local.yml`), `demo/ConfigPrinter.java`.

### Steg 1: kjør uten profil (1 min)

**Hva:** kjør **default**. Les loggen.

**Forventet** *(measured in a real run)*:

```
No active profile set, falling back to 1 default profile: "local"
... ConfigPrinter : @Value demo.greeting: Hello from application-local.yml
... ConfigPrinter : Typed settings: DemoSettings[greeting=Hello from application-local.yml, retries=3]
... ConfigPrinter : Greeter: Hello from the local profile
... ConfigPrinter : Active profiles: [], default profiles: [local]
```

**Hvorfor:** `spring.profiles.default: local` i `application.yml` gjør at `local` blir profilen når ingen ber om en annen. Filen for profilen slår basisfilen.

### Steg 2: spør Environment (2 min)

**Hva:** Postman «One property: ...» eller nettleseren: <http://localhost:8080/actuator/env/demo.greeting>.

**Forventet** *(measured on a real server)*: `property.source` er kilden som vant, og `propertySources` er listen i prioritetsrekkefølge. Listen, for den delen du bryr deg om, er den samme i en reell kjøring som i en test, minus de testspesifikke oppføringene:

```
server.ports
commandLineArgs
servletContextInitParams
systemProperties
systemEnvironment                        <- environment variables
Config resource '... application-local.yml'  value: Hello from application-local.yml (line 2)
Config resource '... application.yml'        value: Hello from application.yml (line 7)
```

Den fullstendige rekkefølgen i `/actuator/env` i en reell kjøring er: `server.ports`, `commandLineArgs`, `servletContextInitParams`, `systemProperties`, `systemEnvironment`, profilfilene, `application.yml`, `applicationInfo`, `Management Server`. (`server.ports` er ny øverst; den inneholder bare portene. `/actuator/env/{navn}` lister i tillegg tomme `servletConfigInitParams` og `random`.) De testspesifikke oppføringene (`Annotations`, `test`, `Inlined Test Properties`) vises ikke i en reell kjøring. Hver oppføring med en verdi har en `origin`, for eksempel `class path resource [application-local.yml] - 2:13`.

**Hvorfor:** `Environment` er en bønne med en liste over `PropertySource`-er. Den første kilden som har navnet vinner. Dette er den samme registry-ideen som i session 1.

### Steg 3: miljøvariabel og kommandolinje (3 min)

**Hva:** i kjøreprofilen **default** setter du miljøvariabelen `DEMO_GREETING=from-env`. Start på nytt. Legg deretter til programargumentet `--demo.greeting=from-args`. Start på nytt igjen. Etter hver omstart ser du på `ConfigPrinter` og `/actuator/env/demo.greeting`.

**Forventet** *(measured)*: med miljøvariabelen er `greeting=from-env` og kilden er `systemEnvironment` (origin `System Environment Property "DEMO_GREETING"`). Med begge vinner kommandolinjen: `greeting=from-args`, kilde `commandLineArgs`, og miljøvariabelen står fortsatt oppført under den.

**Hvorfor:** relaxed binding: `DEMO_GREETING` er miljøvariabel-stavemåten av `demo.greeting`. Rekkefølgen er kommandolinje, deretter miljø, deretter filene. Slik oppfører den samme jar-filen seg ulikt i hvert miljø uten ny bygging.

**Plan B:** backup-slide 11 (listen over kilder).

---

## D2. Typede innstillinger og en lesbar feil (4 min)

**Mål:** `@ConfigurationProperties` gir et typet, validert objekt, og en feil verdi stopper appen med en melding som navngir egenskapen.

### Steg 1: recorden (1 min)

**Hva:** åpne `demo/DemoSettings.java`: en record med `@ConfigurationProperties(prefix = "demo")`, `@Validated`, `@NotBlank String greeting`, `@Min(1) int retries`. Deretter <http://localhost:8080/actuator/configprops/demo>.

**Forventet** *(measured on `session3-demo-done`, default profile)*: én kontekst, `no.course.company.demo.DemoSettings` med `prefix: demo`, `retries = 3` fra `class path resource [application.yml] from company-session3-0.0.1-SNAPSHOT.jar - 8:12` og `greeting = Hello from application-local.yml` fra `class path resource [application-local.yml] from company-session3-0.0.1-SNAPSHOT.jar - 2:13` (med `java -jar` står jar-navnet med; i IntelliJ står det ikke). Samme form gjelder for `registry` på løsningsprosjektet: `properties` med de bundne verdiene, og `inputs` med `origin` for hver verdi, for eksempel `class path resource [application.yml] from company-session3-0.0.1-SNAPSHOT.jar - 7:9` eller `"registry.max-companies" from property source "commandLineArgs"`.

**Hvorfor:** `/actuator/configprops` er den typede visningen; `/actuator/env` er den rå visningen.

### Steg 2: ødelegg det (3 min)

**Hva:** legg til programargumentet `--demo.retries=0` og start.

**Forventet** *(measured on `session3-demo-done`, hele rapporten)*:

```
***************************
APPLICATION FAILED TO START
***************************

Description:

Binding to target no.course.company.demo.DemoSettings failed:

    Property: demo.retries
    Value: "0"
    Origin: "demo.retries" from property source "commandLineArgs"
    Reason: must be greater than or equal to 1

Action:

Update your application's configuration
```

Egenskapsnavnet, verdien, opphavet og årsaken står alle i meldingen. Linjen `Origin:` vises fordi verdien kom fra kommandolinjen.

**Hvorfor:** applikasjonen feiler ved oppstart, ikke klokken tre på natten når verdien brukes. Sammenlign med `@Value("${demo.retries}") int`: ingen validering, og feilen kommer bare hvis verdien ikke kan konverteres.

**Plan B:** backup-slide 12 (feilrapportene).

---

## D3. Samme applikasjon, tre profiler (5 min)

**Mål:** en profil er et navn som velger ekstra konfigurasjonsfiler og ekstra bønner.

### Steg 1: tre kjøringer (3 min)

**Hva:** kjør **test**, deretter **prod** (og **default** er allerede kjent). Se på logglinjene.

**Forventet** *(measured in real runs)*:

```
The following 1 profile is active: "test"
... ConfigPrinter : @Value demo.greeting: Hello from application.yml
... ConfigPrinter : Typed settings: DemoSettings[greeting=Hello from application.yml, retries=1]
... ConfigPrinter : Greeter: Hello from the test profile
... ConfigPrinter : Active profiles: [test], default profiles: [local]

The following 1 profile is active: "prod"
... ConfigPrinter : @Value demo.greeting: Hello from application.yml
... ConfigPrinter : Typed settings: DemoSettings[greeting=Hello from application.yml, retries=5]
... ConfigPrinter : Greeter: Hello from the prod profile
```

`greeting` er `Hello from application.yml` i disse to kjøringene, fordi `application-test.yml` og `application-prod.yml` bare setter `retries`. Standardprofilen `local` er ikke aktiv lenger.

**Merknad for demo-grenen:** `prod` på `session3-demo-done` eksponerer fortsatt `/actuator/env` (`200`), fordi den grenens `application.yml` eksponerer endepunktene for alle profiler. Dette er bevisst: det er «før»-tilstanden. Løsningen eksponerer bare `health` for `prod` (oppgave 3.3, measured: `404` på de andre endepunktene).

**Hvorfor:** `application-{profile}.yml` lastes **i tillegg til** `application.yml`, og verdiene der vinner. `retries` er 3 (base), 1 (test), 5 (prod).

### Steg 2: bønner per profil (2 min)

**Hva:** åpne `LocalGreeter`, `TestGreeter`, `ProdGreeter`: hver har `@Profile("...")`. Bare én av dem finnes per kjøring. Vis det med <http://localhost:8080/actuator/beans> (søk etter `greeter`).

**Forventet** *(measured on `session3-demo-done`)*: `localGreeter` (`no.course.company.demo.LocalGreeter`) med standardprofilen, `testGreeter` med `test`, `prodGreeter` med `prod`. Hver kjøring lister nøyaktig én.

**Hvorfor:** `@Profile` er en betingelse på en bønne, som betingelsene i session 1. Oppgave 3.4 bruker den for å velge mellom e-post- og SMS-senderne.

---

## D4. Hemmeligheter (3 min)

**Mål:** verdier kan skjules i actuatoren, og en manglende verdi må være en høylytt feil, ikke en stille en.

### Steg 1: maskerte verdier (1 min)

**Hva:** kjør **test** (den har ingen `show-values`) og åpne `/actuator/env/demo.greeting`. Kjør deretter **default** (profilen `local` setter `show-values: ALWAYS`) og åpne den igjen.

**Forventet** *(measured with `show-values=NEVER`)*: `"value":"******"`, men `source` og `origin` vises fortsatt. I `local`-kjøringen vises den reelle verdien.

**Hvorfor:** Spring Boot maskerer alle verdier i `/actuator/env` og `/actuator/configprops` som standard. En utviklermaskin kan vise dem, andre miljøer bør ikke. I prosjektet har `local` `show-values: ALWAYS`, og de andre har det ikke.

### Steg 2: en manglende placeholder (2 min)

**Hva:** kjør **broken**. Åpne `demo/BrokenConfiguration.java` først (den ber om `${demo.not-set}`).

**Forventet** *(measured)*: applikasjonen starter ikke. Stacktracen er lang; linjen du skal lese er den siste `Caused by`:

```
Caused by: org.springframework.util.PlaceholderResolutionException: Could not resolve placeholder 'demo.not-set' in value "${demo.not-set}"
```

(For denne feilen skriver Spring Boot ikke ut rapporten `APPLICATION FAILED TO START`, bare unntaket *(measured)*.)

**Hvorfor:** les bunnen av stacktracen først. Og en advarsel for oppgave 3.5: med `@ConfigurationProperties` feiler ikke en uløst `${...}` i en YAML-verdi: den bindes som den bokstavelige teksten `${...}` *(measured, se felles gjennomgang)*. Derfor bruker oppgaven `${NAME:}` pluss `@NotBlank`.

**Plan B:** backup-slide 12.

---

## Tilbakestilling

```
git checkout -- .
git clean -fd
git checkout main
```

For oppgavene bruker deltakerne `session3-start`. For felles gjennomgang bruker du `session3-solution`.

---

## Felles gjennomgang (15 min, `session3-solution`, 15:05)

```
git checkout session3-solution
```

1. **(5 min) Oppgave 3.2, typede innstillinger.** Åpne `RegistryProperties`. Kjør med profilen `test` og `--registry.max-companies=3`: den fjerde POST-en gir `409` med `The register is full: at most 3 companies` *(measured, `CompanyLimitTest`)*. Kjør med `--registry.max-companies=0` og vis rapporten: `Property: registry.maxCompanies`, `Value: "0"`, `Origin: "registry.max-companies" from property source "commandLineArgs"`, `Reason: must be greater than or equal to 1` *(measured)*. Rapporten skriver egenskapsnavnet slik recorden staver det (`registry.maxCompanies`, ikke kebab-case-formen fra kommandolinjen); origin viser stavemåten du skrev.
2. **(4 min) Oppgave 3.5, plassholderfellen.** I `application-prod.yml` er verdien `${NOTIFICATION_API_KEY:}`. Fjern `:` og kjør med `--spring.profiles.active=prod`: applikasjonen **starter** med den bokstavelige teksten `${NOTIFICATION_API_KEY}` som nøkkel *(measured)*. Med `:` og `@NotBlank` feiler den: `Property: registry.notifications.apiKey`, `Value: ""`, `Reason: must not be blank` *(measured)*. Spør: *"Hvorfor er det bedre å feile her?"*
3. **(3 min) Oppgave 3.4 og 3.6.** `SmsNotificationSender` har `@Profile("!prod")`, `EmailNotificationSender` har `@Profile("prod")`. Målte logglinjer: under `test`, `SMS: [Brights Company Register (test)] Registered company: C1`; under `prod` med en nøkkel, `Email from noreply@company.example: [Brights Company Register] Registered company: Company 1`. Hvis du sletter begge senderne, feiler applikasjonen ved oppstart med `Parameter 0 of constructor in no.course.company.service.CompanyService required a bean of type 'no.course.company.bean.NotificationSender' that could not be found.` *(measured)*. Tilleggsoppgave: `REGISTRY_MAX_COMPANIES=9` gir `9` i både `/actuator/configprops/registry` og `/actuator/env/registry.max-companies` (kilde `systemEnvironment`) *(measured)*. `REGISTRY_MAXCOMPANIES=8` binder også: `/actuator/configprops/registry` viser `8` (origin `System Environment Property "REGISTRY_MAXCOMPANIES"`), men `/actuator/env/registry.max-companies` viser fortsatt filverdien `5` fra `application-local.yml` *(measured)*. Bruk `configprops` for sannheten.
4. **(3 min) Dagen.** Tre sesjoner, én idé. Session 1: *bønner lages av en container du kan lese.* Session 2: *en forespørsel er en kallstack gjennom disse bønnene.* Session 3: *konfigurasjon er en stack av kilder du kan inspisere.* Spring er ikke magi. Deretter utsjekk kl. 16:15.

---

## Målte resultater

`mvn verify` (Spring Boot 4.1.2, Temurin 25.0.5). Bare `session3-start` (7 tester, målt på 4.1.2) finnes som kode i repoet; tallene for demo-done og solution er målt på 3.5.16 og er ikke målt på nytt:

| Tag | Tester |
|---|---|
| `session3-start` | 7 (CRUD, validering, grense på 3 selskaper, konteksten laster) |
| `session3-demo-done` | 10 (de 7 over pluss tre profiler, prioritet, ugyldige og manglende verdier) |
| `session3-solution` | 13 (de 7 over pluss 6 om profilvalg, oppstartsfeil, prioritet) |

---

## Det jeg ikke har sett selv (sjekk under øvingen)

- IntelliJ-GUI: kjøreprofiler med profiler, miljøvariabler og programargumenter. Alle kjøringer ble startet fra shellet med `java -jar` og `env`, ikke fra IntelliJ.
- Postman-appens GUI: samlingen ble kjørt med `newman` (kommandolinjemotoren i Collection Runner), ikke klikket i appen.
- Windows: syntaksen for miljøvariabler og argumenter er en annen i `cmd` og PowerShell.
- Slidene: `materials/build_session3_slides.py` (13 slides) er sjekket med et estimat for tekstplass og en Quick Look-rendring av hvert slide. Not checked: PowerPoint itself (its PDF export did not finish in this session).

---

## Slides og demoer: når du bytter

Presentasjonen er `session3-slides.pptx` (13 slides, bygget av `materials/build_session3_slides.py`). Talenotatene på hvert slide sier hva du skal si og hvor du bytter.

| Tid | Modus | Slides / demo | Min |
|---|---|---|---|
| 13:20 | Slides | 1 Tittel, 2 Agenda, 3 Mål (repetisjon), 4 Oppsettsjekk | 4 |
| 13:24 | Slides | 5 Mange kilder, én vinner | 2 |
| 13:26 | **Demo 1** (IntelliJ, nettleser) | Hvor kommer denne verdien fra? | 6 |
| 13:32 | Slides | 6 Typede innstillinger | 1 |
| 13:33 | **Demo 2** (IntelliJ) | `@ConfigurationProperties` og en lesbar feil | 4 |
| 13:37 | Slides | 7 Profiler | 2 |
| 13:39 | **Demo 3** (IntelliJ) | Samme applikasjon, tre profiler | 5 |
| 13:44 | Slides | 8 Hemmeligheter havner aldri i repoet | 1 |
| 13:45 | **Demo 4** (IntelliJ, nettleser) | Maskerte verdier, manglende placeholder | 3 |
| 13:48 | Slides | 9 Feil du vil møte | 1 |
| 13:49 | Slides | 10 Din tur | 1 |
| 13:50 | Oppgaver | deltakerne jobber (slide 10 står oppe) | 75 |
| 15:05 | Gjennomgang | felles gjennomgang på `session3-solution` | 15 |

Backup-slides: 11 (liste over kilder, hvis `/actuator/env` feiler i demo 1), 12 (feilrapportene, hvis demo 2 eller 4 feiler), 13 (navn på miljøvariabler, for tilleggsoppgaven).
