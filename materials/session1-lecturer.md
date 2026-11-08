# Økt 1: foreleserens manus (steg, hva og hvorfor)

Dette er stegene du gjør foran salen, i rekkefølge. For hvert steg får du **hva** du gjør, **hva som skjer**, og **hvorfor** vi gjør det (budskapet til deltakerne).

Økt 1 går fra 08:00 til 10:40, med lang kaffepause 09:00–09:30 midt i oppgavetiden (mange av deltakerne må gjøre et stykke arbeid da). Innsjekk 08:00–08:15, teori og demoer 08:15–08:45, oppgaver 08:45–09:00 og 09:30–10:30, felles gjennomgang 10:30–10:40. Lunsj 10:40–11:10. Dagsplanen finnes i `materials/day1-schedule.md`.

Verdier merket *(målt)* er observert ved kjøring av prosjektet (Spring Boot 4.1.2, Temurin JDK 25.0.5, Maven, 8. nov 2026; `java -jar`, `curl`, `jdb` og kjøring av `MiniApp`). Punkter merket *(ikke sjekket)* har jeg ikke fått kjørt i IntelliJ-GUI-et.

---

## Den røde tråden

Deltakerne kan allerede skrive Spring Boot. Poenget med økten er å vise dem at Spring **ikke er magi**:

1. D1: en DI-container er skanning + `new` via reflection + et register. (Rundt 75 linjer kode.)
2. D2: i debuggeren ser vi nøyaktig når og hvordan Spring lager *våre* objekter.
3. D3: det som «bare virker», kommer fra autokonfigurasjon, styrt av hva som ligger på classpath, og vi kan lese begrunnelsen.

Aha-øyeblikket: *«Jeg kan selv løfte på panseret, og det Spring gjør, er til å forstå.»*

---

## Før du starter (dagen før og 10 min før økten)

1. Åpne prosjektet `company-session1` i IntelliJ. Maven-vinduet skal vise prosjektet (høyreklikk `pom.xml` → *Maven → Sync Project* hvis ikke). SDK og language level skal være 25.
2. Lag en egen branch for demoen, så du kan forkaste alle endringer etterpå:

   ```
   git checkout -b demo session1-demo-done
   ```

   **Hvorfor:** du endrer kode live (legger til klasser, kommenterer ut ting i `pom.xml`). Med en branch kan du alltid gå tilbake til ferdig tilstand med `git checkout -- .` og `git clean -fd`. Taggen `session1-demo-done` er nødplanen din hvis livekodingen går galt.
3. Last inn Maven på nytt etter at du har byttet tag/branch (*Sync Project*).
4. Kjør gjennom alle tre demoene én gang på forhånd.
5. Ta skjermbilder som plan B: call stacken fra D2 og conditions-rapporten fra D3.
6. Port 8080 må være ledig (stopp andre apper som kjører).
7. Bruk stor skrift i IDE og konsoll. Tøm konsollen før hver demo.

---

## D1. Spring uten Spring (7 min)

**Mål:** vise at en DI-container er enkel nok til å forstås helt. Når de har sett det, blir Spring mindre mystisk.

Filer: `src/main/java/no/course/company/minicontainer/` (`MiniContainer`, `MiniComponent`) og `minicontainer/demo/` (`MiniApp`, `NotificationSender`, `EmailNotificationSender`, `CompanyService`).

### Steg 1: problemet (1 min)

**Hva:** vis (helst på slide S1) at uten DI skriver vi `new CompanyService(new EmailNotificationSender())` i hver klasse som trenger den.
**Hvorfor:** deltakerne må kjenne på problemet: hva skjer med 50 klasser, og hva om e-post skal byttes ut med SMS?

### Steg 2: les MiniContainer (3 min)

**Hva:** åpne `MiniContainer.java` og pek på de tre kommentarene i koden:

| Del | Hva koden gjør | Hva du sier |
|---|---|---|
| **1. SCAN** | Leser alle `.class`-filer i en pakke og beholder dem med `@MiniComponent` | «Dette er `@ComponentScan`.» |
| **2. CREATE** | Lager en instans av hver kandidat | «Dette er bean-opprettelse.» |
| **3. INJECT** | `getConstructors()[0]` gir konstruktøren, `getParameterTypes()` gir avhengighetene. Disse lages først, rekursivt. | «Konstruktøren er oppskriften. Parameterne er avhengighetene.» |

I tillegg: et `Map` som lagrer instansene (singleton: det samme objektet gjenbrukes), og et `Set` som oppdager sirkulære avhengigheter.

**Hvorfor:** dette er hele ideen. Ekte Spring gjør det samme, pluss scopes, livssyklus, proxyer og autokonfigurasjon.

### Steg 3: kjør MiniApp (1 min)

**Hva:** høyreklikk `MiniApp` → *Run*.

**Forventet output** *(målt)*:

```
Creating EmailNotificationSender
Creating CompanyService
Email: Registered company: Acme Corp
Beans: [class no.course.company.minicontainer.demo.EmailNotificationSender, class no.course.company.minicontainer.demo.CompanyService]
```

**Hvorfor / forklar:** `CompanyService` skulle lages, men trenger en `NotificationSender`. Containeren lager den først (derfor kommer `EmailNotificationSender` først). Det er *avhengigheter først*, som i ekte Spring.

### Steg 4: to kandidater (1 min)

**Hva:** kopier `EmailNotificationSender` til en ny klasse `SmsNotificationSender` i samme pakke (`minicontainer/demo`), la den implementere `NotificationSender`, gi den `@MiniComponent`, og la `send` skrive ut «SMS: ...». Kjør `MiniApp`.

**Forventet** *(målt)*:

```
Exception in thread "main" java.lang.IllegalStateException: Multiple beans of type NotificationSender: [class ...EmailNotificationSender, class ...SmsNotificationSender]
```

Hvis `EmailNotificationSender` rekker å bli laget først, står `Creating EmailNotificationSender` over feilen. Det kommer an på rekkefølgen klassefilene leses i (den er ikke garantert). I målingen kom ingen `Creating`-linje før feilen.

**Hvorfor:** dette er `NoUniqueBeanDefinitionException` i ekte Spring. Containeren vet ikke hvilken den skal velge. Deltakerne møter akkurat denne feilen i oppgave 1.3. Slett `SmsNotificationSender` igjen.

*(Test fra IntelliJ-GUI-et: ikke sjekket, men logikken er kjørt og gir meldingen over.)*

### Steg 5: sirkulær avhengighet (1 min)

**Hva:** lag to klasser i `minicontainer/demo`, begge `@MiniComponent`:

```java
@MiniComponent public class A { public A(B b) { } }
@MiniComponent public class B { public B(A a) { } }
```

Kjør `MiniApp`.

**Forventet** *(målt)*: `IllegalStateException: Circular dependency at A`.

**Hvorfor:** A trenger B, som trenger A. Ingen av dem kan lages først. Constructor injection gir denne feilen **ved oppstart**, ikke midt i produksjon. Slett A og B igjen.

### Steg 6: bro over til ekte Spring

**Hva du sier:** «Ekte Spring gjør det samme, og mye mer: scopes, livssyklus, proxyer, autokonfigurasjon. Men kjernen er dette.»

**Plan B (hvis tiden er knapp):** hopp rett til steg 6 og forklar ideen på S1. Kutt D1 først hvis du ligger etter skjema.

**Kjent begrensning:** skanningen leser klassefiler fra en mappe, så den virker fra IDE-en og `target/classes`, ikke fra en pakket jar.

---

## D2. Se beans bli laget: debugger og Actuator (5 min)

**Mål:** vise *når* og *hvordan* Spring lager objektene våre, og hvor mange beans det er totalt.

### Steg 1: breakpoint

**Hva:** åpne `service/CompanyService.java`. Sett et breakpoint (klikk i margen) på første linje i konstruktøren, `log.info("CompanyService is being created")`. Start `CompanyApplication` med **Debug** (insekt-ikonet).
**Hva skjer:** appen stopper under oppstart, **før** port 8080 åpnes.
**Hvorfor:** vi stopper akkurat når Spring kaller *vår* konstruktør.

### Steg 2: les call stacken

**Hva:** i *Debug*-vinduet åpner du **Frames** (fanen *Threads & Variables*). Les ovenfra og ned.
**Viktig:** IntelliJ skjuler bibliotek-frames som standard. Først ser du bare tre rader: `<init>:21, CompanyService`, **`31 hidden frames`** og `main:10, CompanyApplication` *(målt)*. Klikk på raden **«31 hidden frames»** for å utvide den (eller slå av «Hide Frames from Libraries» i filter-/⋮-menyen i Debug-vinduet, nøyaktig navn ikke sjekket). De 31 skjulte framene er JDK-ens reflection-maskineri pluss Spring, og det er nettopp poenget: «Alt imellom er Spring (og litt reflection).»
**Forventet** *(hele stacken målt med `jdb` på Boot 4.1.2, men ikke sett i IntelliJ-GUI-et)*:

```
CompanyService.<init>:21
 -- JDK reflection (java.lang.invoke / java.lang.reflect), 7 rows measured:
    DirectMethodHandle$Holder.newInvokeSpecial
    LambdaForm$MH.invoke
    Invokers$Holder.invokeExact_MT
    DirectConstructorHandleAccessor.invokeImpl:87
    DirectConstructorHandleAccessor.newInstance:62
    Constructor.newInstanceWithCaller:499
    Constructor.newInstance:483
BeanUtils.instantiateClass:207
SimpleInstantiationStrategy.instantiate:129
ConstructorResolver.instantiate:318
ConstructorResolver.autowireConstructor:309
...AbstractAutowireCapableBeanFactory.autowireConstructor / createBeanInstance / doCreateBean / createBean
...AbstractBeanFactory.doGetBean / getBean (via DefaultSingletonBeanRegistry.getSingleton)
DefaultListableBeanFactory.instantiateSingleton / preInstantiateSingleton(s)
...AbstractApplicationContext.finishBeanFactoryInitialization
...AbstractApplicationContext.refresh
ServletWebServerApplicationContext.refresh:131
SpringApplication.refresh / refreshContext / run
CompanyApplication.main:10
```

Reflection-radene øverst kommer fra JDK-en (de varierer mellom JDK-versjoner) og er ikke Spring. Be salen hoppe over dem og begynne å lese ved `BeanUtils.instantiateClass`. Spring-klassenavn og linjenumre varierer litt mellom versjoner. Noter det du faktisk ser, og lagre et skjermbilde som plan B.
**Hva du sier:** «Her er `main`-metoden din, og her er konstruktøren din. Alt imellom er Spring.»
**Hvorfor:** viser at dette er helt vanlig Java-kode i en kjede, ikke magi.

### Steg 3: `sender`-parameteren

**Hva:** klikk på øverste frame, `<init>:21, CompanyService`. Variablene vises i høyre halvdel av fanen *Threads & Variables* (hvis en annen frame, for eksempel «31 hidden frames», er valgt, står det «Variables are not available» *(målt)*). Gjør Debug-vinduet bredt nok til at du ser begge halvdelene. Åpne parameteren `sender`. Alternativ: skriv `sender` i feltet *Evaluate expression* og trykk Enter.
**Forventet:** den er allerede en `EmailNotificationSender`.
**Hvorfor:** Spring laget den *før* `CompanyService`, fordi `CompanyService` trenger den. Samme rekkefølge som i minicontaineren.

### Steg 4: Resume og Actuator

**Hva:** trykk Resume (F9). Åpne `http://localhost:8080/actuator/beans` i nettleseren.
**Forventet** *(målt)*: i JSON-en har `contexts` → **`company-session1`** → `beans` → `companyService` `scope: singleton` og `dependencies: ["emailNotificationSender"]`.
**Merk:** nøkkelen er appnavnet (`company-session1`), ikke `application` som i planen.
**Hvorfor:** Actuator er det andre verktøyet deltakerne skal bruke selv. Her ser vi hele registeret og avhengighetene.

### Steg 5: tell beans

**Hva:** les konsollen, linjen `Total number of beans: N` fra `BeanCounterRunner`. Forvent et sted mellom 300 og 400 beans. Alternativt kan du åpne `/actuator/beans`.
**Hva du sier:** «Vi skrev 4–5 klasser selv. Resten laget Spring Boot for oss.»
**Hvorfor:** viser omfanget av autokonfigurasjonen.

Egne beans i `no.course`: 5 i demo-done (`companyApplication`, `companyController`, `companyService`, `emailNotificationSender`, `beanCounterRunner`).

**Plan B:** hvis debuggeren er treg, vis skjermbildet av call stacken og gå rett til steg 4.

---

## D3. `--debug`: hvorfor kjører Tomcat? (5 min)

**Mål:** vise at autokonfigurasjon er *betinget* av classpath, og at vi kan lese begrunnelsen. Deretter bytte webserver uten å endre Java-kode.

### Steg 1: start med `--debug`

**Hva:** *Run → Edit Configurations → CompanyApplication → Modify options → Program arguments*, skriv `--debug`. Kjør.
**Hva skjer:** konsollen får en stor `CONDITIONS EVALUATION REPORT`-blokk.
**Hvorfor:** dette er det tredje verktøyet. Rapporten forteller hvorfor hver autokonfigurasjon ble tatt med eller ikke.

### Steg 2: finn Tomcat

**Hva:** søk (Ctrl+F i konsollen) etter `TomcatServletWebServerAutoConfiguration` under **Positive matches**. (På Spring Boot 3 het den `ServletWebServerFactoryConfiguration.EmbeddedTomcat`. Den finnes ikke lenger.)
**Forventet** *(målt)*:

```
TomcatServletWebServerAutoConfiguration matched:
   - @ConditionalOnClass found required classes 'jakarta.servlet.ServletRequest', 'org.apache.catalina.startup.Tomcat', 'org.apache.coyote.UpgradeProtocol', 'org.springframework.boot.tomcat.servlet.TomcatServletWebServerFactory' (OnClassCondition)
   - found 'session' scope (OnWebApplicationCondition)
```

**Hva du sier:** «Tomcat ligger på classpath, så Spring Boot lager en Tomcat. Det er hele trikset.»

### Steg 3: DataSource

**Hva:** søk etter `DataSourceAutoConfiguration` / `Hikari`.
**Forventet** *(målt)*: `DataSourceAutoConfiguration` og `DataSourceConfiguration.Hikari` er matched, fordi H2 og Hikari ligger på classpath og ingen har laget en egen `DataSource` (`@ConditionalOnMissingBean ... did not find any beans`).
**Hvorfor:** peker fram mot oppgave 1.5 (deltakerne finner ut hvem som laget `dataSource`).

### Steg 4: Negative matches

**Hva:** scroll ned til **Negative matches**, finn f.eks. `PrometheusMetricsExportAutoConfiguration`. (`JmsAutoConfiguration` står ikke i rapporten på Boot 4, fordi JMS-modulen ikke er på classpath i det hele tatt.)
**Forventet** *(målt)*: `@ConditionalOnClass did not find required class 'io.micrometer.prometheusmetrics.PrometheusMeterRegistry'`.
**Hva du sier:** «Samme mekanisme, bare motsatt vei: Prometheus-biblioteket ligger ikke på classpath, så ingenting skjer.»

### Steg 5: bytt til Jetty

**Hva:** åpne `pom.xml`. Der er det to utkommenterte blokker merket «Demo D3»: en `<exclusions>`-blokk inne i `spring-boot-starter-web`, og en `spring-boot-starter-jetty`-avhengighet rett under. Fjern de fire kommentarlinjene (`<!-- Demo D3: ...` og `-->` for hver blokk) så begge blokkene blir aktive. Maven *Sync Project*. Start appen (fortsatt med `--debug`).

**Forventet** *(målt)*:
- Logg: `Jetty started on port 8080 (http/1.1)`
- Rapporten: `JettyServletWebServerAutoConfiguration matched` under positive. `TomcatServletWebServerAutoConfiguration` er helt borte fra rapporten (Tomcat-modulen er ikke på classpath lenger).
- `/actuator/health` er `UP`.

**Hva du sier:** «Jeg endret ingen Java-kode. Jeg endret classpath.»
**Hvorfor:** det er beviset på at classpath bestemmer hva du får.

### Steg 6: sett det tilbake

**Hva:** `Ctrl+Z` eller `git checkout pom.xml`, Sync Project.

**Plan B:** hopp over steg 5–6 hvis tiden er knapp. Steg 1–4 er kjernen.

---

## Etter demoene: tilbakestill

```
git checkout -- .
git clean -fd
git checkout main
```

(Forkaster alle live-endringer i `demo`-branchen. Taggene er urørt.)

Til oppgavene bruker deltakerne `session1-start`. Til den felles gjennomgangen bruker du `session1-solution`.

---

## Felles gjennomgang (10 min, `session1-solution`, 10:30)

```
git checkout session1-solution
```

1. **(4 min) Oppgave 1.3 live.** Åpne `CompanyService`. Fjern `@Qualifier("emailNotificationSender")` fra konstruktøren, start appen og vis feilmeldingen (se `session1-tasks.md` for teksten). Sett den tilbake. Spør: *«Hva er forskjellen på `@Primary` og `@Qualifier`? Når bruker du hva?»* Svar: `@Primary` = standardvalg. `@Qualifier` = et bestemt valg der det injiseres.
2. **(3 min) Oppgave 1.5.** La en deltaker forklare hva hen fant. Svar: `DataSourceConfiguration$Hikari`, valgt av `DataSourceAutoConfiguration`.
3. **(3 min) Spørsmål.** Typisk: *«Hvorfor ikke `@Autowired` på felter?»* (vanskelig å teste, skjuler avhengigheter, kan ikke være `final`.) *«Er beans alltid singletons?»* (Ja som standard, men de er ikke trådsikre av seg selv.)

Bro over til økt 2: *«Nå vet vi hvordan Spring lager objektene. Neste: hva skjer når en HTTP-request treffer dem?»*

---

## Det jeg ikke har sett selv (sjekk under generalprøven)

- Alt i IntelliJ-GUI-et (Debug-knappen, Frames, run configuration med `--debug`).
- Den faktiske call stacken i D2.
- At appen kjører uten problemer på en installert JDK 25 (jeg kjørte på Temurin 25.0.5).

---

## Slides og demoer: når du bytter

Decket er `session1-slides.pptx` (13 slides). Speaker notes på hver slide sier hva du skal si og hvor du bytter.

| Tid | Modus | Slides / demo | Min |
|---|---|---|---|
| 08:00 | Slides | 1 Tittel, 2 Agenda, 3 Mål, 4 Oppsettsjekk, 5 Tre verktøy | 15 |
| 08:15 | Slides | 6 Hvorfor DI? | 2 |
| | **Demo 1** (IntelliJ) | Spring uten Spring: MiniApp, MiniContainer | 7 |
| | Slides | 7 ApplicationContext, 8 @SpringBootApplication, 9 Oppstartsløpet | 7 |
| | **Demo 2** (IntelliJ) | Debugger og Actuator | 5 |
| | Slides | 10 Starters og autokonfigurasjon | 2 |
| | **Demo 3** (IntelliJ) | `--debug` conditions report, bytte til Jetty | 5 |
| | Slides | 11 Livssyklus og scopes, 12 Feil, 13 Din tur | 2 |
| 08:45 | Oppgaver | deltakerne kommer i gang (slide 13 står oppe), mål: 1.1 før pausen | 15 |
| 09:00 | **Kaffepause** | lang pause, mange må gjøre et stykke arbeid. Start presis 09:30 | 30 |
| 09:30 | Oppgaver | deltakerne fortsetter med 1.2–1.5, 1.6 for dem som blir ferdige | 60 |
| 10:30 | Gjennomgang | felles gjennomgang på `session1-solution` | 10 |

Tre runder fram og tilbake: slides, demo, slides, demo, slides, demo, slides. Mellom demo 1 og demo 2 er det én sammenhengende slide-blokk (slides 7–9, rundt 7 min).
Ingen reserveslides. Svikter debuggeren i demo 2, les de øverste radene i call stacken fra manuset (D2 steg 2) og gå videre til `/actuator/beans`. Svikter `--debug` i demo 3, vis `/actuator/conditions` i nettleseren.
