# Økt 1: oppgaver

Du skal bruke verktøyene fra demoene selv, og bygge litt. Hver oppgave har **Mål** (hva), **Hvorfor** (hva du lærer), **Slik gjør du det** og **Ferdig når**.

Dokumentet har to typer innhold:
- Vanlig tekst er oppgaveteksten deltakerne får.
- Bokser merket **🔎 For validering** er for deg som validerer oppsettet. De fjernes før oppgavene deles ut.

---

## Før du starter

1. Ha JDK 25, IntelliJ og Maven. (Maven følger med IntelliJ.)
2. Klon repoet og sjekk ut `session1-start`:

   ```
   git checkout -b my-answers session1-start
   ```

   **Hvorfor en branch:** du kommer til å endre kode, og vil kunne sammenligne med fasiten uten å miste noe.
3. Åpne prosjektet i IntelliJ. Maven-vinduet skal vise `company-session1` (høyreklikk `pom.xml` → *Maven → Sync Project* ved behov).
4. Start `CompanyApplication` (grønn pil). Åpne <http://localhost:8080/actuator/health>. Du skal se `{"status":"UP"}`.

Verktøyene du skal bruke:
- **Debugger**: breakpoint i konstruktøren til `CompanyService`.
- **Actuator**: <http://localhost:8080/actuator/beans>
- **`--debug`**: *Run → Edit Configurations → Program arguments*, skriv `--debug`. Gir en `CONDITIONS EVALUATION REPORT` i konsollen.

> **🔎 For validering:** `session1-start` skal gi `{"status":"UP"}` *(målt)*, ha `companyService` og `emailNotificationSender` i `/actuator/beans` *(målt)* og ikke ha `smsNotificationSender` *(målt)*. I tillegg viser `git diff session1-start session1-solution` alt oppgavene krever, hvis du vil se kodefasiten.

---

## Oppgave 1.1: Oppstartsdetektiv

**Mål:** kjør appen og les loggen. Svar på fem spørsmål:
1. Hvilken port?
2. Hvilken webserver, og hvilken versjon?
3. Hvilken profil er aktiv?
4. Hvor lang tid tok oppstarten?
5. Hva slags database og hvilken URL bruker Hikari?

**Hvorfor:** loggen forteller mye om hva Spring Boot gjør. Du bør lære deg å lese den, ikke bare scrolle forbi.

**Slik gjør du det:** start appen og søk i konsollen (Ctrl+F) etter `Tomcat`, `profile`, `Started` og `Hikari`.

**Ferdig når:** du har svart på alle fem ut fra loggen.

> **🔎 For validering** *(målt på Boot 4.1.2, 8. nov)*:
>
> | Spørsmål | Svar | Logglinje |
> |---|---|---|
> | Port | 8080 | `Tomcat started on port 8080 (http) with context path '/'` |
> | Webserver | Apache Tomcat 11.0.27 | `Starting Servlet engine: [Apache Tomcat/11.0.27]` |
> | Profil | ingen, `default` | `No active profile set, falling back to 1 default profile: "default"` |
> | Oppstartstid | ca. 2 sekunder (kan bli 3–8 s på første kjøring eller tregere maskiner) | `Started CompanyApplication in N seconds` |
> | Database | H2 in-memory, `jdbc:h2:mem:<uuid>`, pool `HikariPool-1` | `HikariPool-1 - Added connection conn0: url=jdbc:h2:mem:...` |
>
> Tomcat-versjonen følger Spring Boot-versjonen og kan endre seg ved en patch-oppdatering.

---

## Oppgave 1.2: Bygg og tell

**Mål:**
- Lag `OrgNumberValidator` som en `@Component` og injiser den i `CompanyService` via konstruktøren.
- Lag `BeanCounterRunner` (en `CommandLineRunner`) som bruker `ApplicationContext` til å skrive ut **totalt antall beans** og **navnene på beans i pakken `no.course`**.

**Hvorfor:**
- Når du lager og injiserer din egen bean, ser du hele DI-kjeden: du annoterer, Spring skanner, lager og injiserer.
- Runneren viser at registeret av beans er noe du kan spørre mot. Du ser også at Spring Boot har laget hundrevis av beans du aldri har skrevet.

**Slik gjør du det:**
- Begge klassene finnes som tomme stubber i `tasks/`, med en TODO-kommentar.
- `OrgNumberValidator`: legg til `@Component`, lag en metode `boolean isValid(String organizationNumber)`. En enkel sjekk holder (for eksempel at organisasjonsnummeret er nøyaktig 9 sifre).
- Gi `CompanyService` en ekstra konstruktørparameter `OrgNumberValidator`. Bruk den i `register(...)` (for eksempel logg en advarsel hvis nummeret er ugyldig. Ikke avvis noe ennå, validering kommer i økt 2).
- `BeanCounterRunner`: `@Component`, implementer `CommandLineRunner`, få `ApplicationContext` gjennom konstruktøren. Se på `getBeanDefinitionCount()`, `getBeanDefinitionNames()` og `getType(name)`. Filtrer på typer der pakkenavnet starter med `no.course`.

**Ferdig når:** outputen ved oppstart viser antall beans og dine egne beans, inkludert `orgNumberValidator`.

> **🔎 For validering** *(målt i `session1-solution`)*:
> ```
> Total number of beans: N   (between 300 and 400)
> Own bean: beanCounterRunner
> Own bean: clockConfig
> Own bean: companyApplication
> Own bean: companyController
> Own bean: companyService
> Own bean: emailNotificationSender
> Own bean: lifecycleBean
> Own bean: orgNumberValidator
> ... (etc.)
> ```
> Totalen blir mellom 300 og 400 beans. Egne beans etter 1.2: de fire fra start, `beanCounterRunner` og `orgNumberValidator`. Filteret på `no.course` tar også med `companyApplication`, og det er riktig.
>
> Målt på Boot 4.1.2: `getBeanDefinitionCount()` gir 363 rett etter 1.2 og 370 når 1.3–1.4 også er gjort. `/actuator/beans` viser 338 på start. Tallene er ulike fordi actuator teller de laget singleton-instansene, mens `getBeanDefinitionCount()` teller definisjoner. Godt spørsmål å ta i plenum.
>
> Merk: `clock`-beanen fra 1.4 vises **ikke** i lista, fordi typen er `java.time.Clock`, ikke `no.course...`. Det er riktig oppførsel og et fint poeng om hva filteret faktisk filtrerer på.
>
> Fasit i `src/main/java/no/course/company/tasks/OrgNumberValidator.java` og `BeanCounterRunner.java` på taggen `session1-solution`.

---

## Oppgave 1.3: To kandidater

**Mål:** gjør `SmsNotificationSender` til en `@Component`. Start appen, **les feilmeldingen**, og løs det først med `@Primary`, deretter med `@Qualifier`.

**Hvorfor:** `NoUniqueBeanDefinitionException` er en av de vanligste Spring-feilene. Når du har lest den, sett hvilken hjelp den gir og løst den på to måter, kjenner du den igjen i produksjon. Du ser også forskjellen på et «standardvalg» og et «bestemt valg».

**Slik gjør du det:**
1. `SmsNotificationSender` er foreløpig en tom klasse. La den implementere `NotificationSender` og legg til `@Component`. Logg for eksempel `"SMS: " + message`.
2. Start appen. Den skal **feile**. Les hele meldingen under `APPLICATION FAILED TO START` og finn ut: hvilken klasse feilet, hvilke to beans ble funnet, og hva foreslår Spring selv?
3. Løsning A: sett `@Primary` på én av senderne. Start. Registrer et selskap (POST til `/api/companies`) og se i loggen hvilken sender som brukes.
4. Løsning B: fjern `@Primary` og bruk `@Qualifier("emailNotificationSender")` på parameteren i konstruktøren til `CompanyService`. Start på nytt og sjekk loggen.

**Ferdig når:** appen starter og `CompanyService` får senderen du valgte.

Test med:

```
curl -X POST -H "Content-Type: application/json" -d "{\"organizationNumber\":\"123456789\",\"name\":\"Acme Corp\"}" -i http://localhost:8080/api/companies
```

> **🔎 For validering** *(målt)*:
>
> Feilmeldingen uten fiks *(målt på Boot 4.1.2)*:
> ```
> APPLICATION FAILED TO START
> Description:
> Parameter 0 of constructor in no.course.company.service.CompanyService required a single bean, but 2 were found:
>    - emailNotificationSender: defined in URL [...EmailNotificationSender.class]
>    - smsNotificationSender: defined in URL [...SmsNotificationSender.class]
> Action:
> Consider marking one of the beans as @Primary, updating the consumer to accept multiple beans, or using @Qualifier to identify the bean that should be consumed
> Ensure that your compiler is configured to use the '-parameters' flag.
> ```
> Meldingen sier også «This may be due to missing parameter name information». Det er generisk tekst, ikke årsaken her.
>
> Med `@Primary` på `SmsNotificationSender`: POST gir `201` og loggen viser `SMS: Registered company: Acme Corp`. *(målt)*
> Med `@Qualifier("emailNotificationSender")` i `CompanyService`: loggen viser `Email: ...` *(fasit i `session1-solution`, kjørt med tester)*.
>
> Merk: stubben `SmsNotificationSender` i `session1-start` er en tom klasse som ikke implementerer `NotificationSender`. Deltakerne må selv legge til `implements NotificationSender` og `send(...)`.

---

## Oppgave 1.4: Livssyklus og scopes

**Mål:**
1. Lag en `@Configuration` som eksponerer en `Clock`-bean (`@Bean`).
2. Lag en bean med `@PostConstruct` og `@PreDestroy` som logger i hver av dem.
3. Lag en bean med `@Scope("prototype")`, injiser den to steder, og sammenlign instansene med `==`.
4. Gjør det samme med en vanlig (singleton) bean.

**Hvorfor:**
1. `@Bean` er måten å registrere klasser du ikke eier selv (som `Clock`). Det er også slik autokonfigurasjon lager beans.
2. Livssyklus: konstruktør → injeksjon → `@PostConstruct` → bruk → `@PreDestroy`.
3. Scope bestemmer hvor mange instanser Spring lager. Singleton er standard og deles overalt. Prototype gir en ny instans per injeksjonspunkt.

**Slik gjør du det:**
- `ClockConfig` og `PrototypeExample` finnes som tomme stubber i `tasks/`.
- `ClockConfig`: `@Configuration`, og en metode `Clock clock()` med `@Bean` som returnerer `Clock.systemDefaultZone()`.
- Livssyklus-bean: en ny klasse med `@Component` og to metoder, annotert med `@PostConstruct` og `@PreDestroy` (fra `jakarta.annotation`). Logg i konstruktøren også, så du ser rekkefølgen.
- `PrototypeExample`: `@Component` og `@Scope("prototype")`. Lag en annen klasse (for eksempel en `CommandLineRunner`) som får den injisert **to ganger** i konstruktøren, og skriv ut `a == b`.
- Gjør det samme med en vanlig `@Component` (en ny klasse), og sammenlign.

**Ferdig når:** du kan forklare hvorfor prototype-instansene er forskjellige og singleton-instansene er de samme.

> **🔎 For validering** *(målt i `session1-solution`)*:
> ```
> LifecycleBean: constructor
> LifecycleBean: @PostConstruct
> ...
> prototype1 == prototype2: false
> singleton1 == singleton2: true
> ```
> `@PreDestroy` *(målt på Boot 4.1.2)*: når appen stoppes med SIGTERM (det samme som *Stop* i IntelliJ), logges `LifecycleBean: @PreDestroy`. Blir appen drept hardt (`kill -9`), kommer linjen ikke.
>
> Et faktum til diskusjon: Spring kaller **ikke** `@PreDestroy` på prototype-beans, siden containeren ikke holder på dem etter at de er laget. *(Målt: en `@PreDestroy` på `PrototypeExample` ble aldri kalt.)*

---

## Oppgave 1.5: Hvem laget `DataSource`?

**Mål:** finn ut **hvilken klasse** og **hvilken autokonfigurasjon** som laget beanen `dataSource`, og **hvorfor**. Bruk minst to av tre metoder:
1. `/actuator/beans`
2. `--debug`-rapporten
3. `getBeanDefinition("dataSource").getResourceDescription()` i kode

**Hvorfor:** vi har aldri skrevet en `DataSource`, men den finnes. Du finner «hvem» og «hvorfor» selv. Det er dette detektivarbeidet som gjør at du kan debugge uventet oppførsel i Spring Boot.

**Slik gjør du det:**
- **Actuator:** åpne `/actuator/beans`, finn `contexts` → `company-session1` → `beans` → `dataSource`. Se på feltene `type`, `resource` og `dependencies`.
- **`--debug`:** søk i rapporten etter `DataSourceAutoConfiguration` og `Hikari`, og les begrunnelsen («matched ...»).
- **Kode (valgfritt):** i en `CommandLineRunner` med `ApplicationContext`, cast til `ConfigurableApplicationContext` og kall `getBeanFactory().getBeanDefinition("dataSource").getResourceDescription()`.

**Ferdig når:** du kan peke på riktig konfigurasjonsklasse og begrunnelsen.

> **🔎 For validering:**
>
> Svar: beanen ble laget av `DataSourceConfiguration$Hikari` (i `org.springframework.boot.jdbc.autoconfigure` på Boot 4; på Boot 3 het pakken `org.springframework.boot.autoconfigure.jdbc`), valgt av `DataSourceAutoConfiguration`. Begrunnelse: H2 gir en innebygd database, HikariCP ligger på classpath, og vi har ikke laget vår egen `DataSource`-bean.
>
> Målt output fra `/actuator/beans` *(Boot 4.1.2)*:
> ```
> type: com.zaxxer.hikari.HikariDataSource
> scope: singleton
> resource: class path resource [org/springframework/boot/jdbc/autoconfigure/DataSourceConfiguration$Hikari.class]
> dependencies: [..DataSourceConfiguration$Hikari, spring.datasource-..DataSourceProperties, jdbcConnectionDetails, environment]
> ```
> I `--debug`-rapporten *(målt)*: `DataSourceAutoConfiguration.PooledDataSourceConfiguration matched: ... @ConditionalOnMissingBean (types: javax.sql.DataSource,javax.sql.XADataSource ...) did not find any beans` og `DataSourceConfiguration.Hikari matched: @ConditionalOnClass found required class 'com.zaxxer.hikari.HikariDataSource'`. Den første linjen er selve «hvorfor»: ingen egen `DataSource`.
>
> Kodevarianten `getResourceDescription()` *(målt)* gir nøyaktig samme tekst som `resource` over.

---

## Oppgave 1.6: Stretch (valgfritt)

**Mål:** velg én eller flere:
- (a) Bytt til Jetty (hvis du ikke gjorde det i demoen).
- (b) Lag en bean som bare opprettes når `course.notifications.enabled=true` (`@ConditionalOnProperty`).
- (c) Utvid minicontaineren med singleton vs. prototype, eller støtte for `@Qualifier`.

**Hvorfor:** (a) beviser at classpath bestemmer. (b) er det samme prinsippet som autokonfigurasjon bruker. (c) viser at du forstår containeren godt nok til å utvide den.

> **🔎 For validering:** (a) er målt på Boot 4.1.2: med `spring-boot-starter-tomcat` ekskludert og `spring-boot-starter-jetty` lagt til starter appen på `jetty-12.1.14` (`Jetty started on port 8080 (http/1.1)`), og `/actuator/health` er `UP`. (b) er målt: en `@Component` med `@ConditionalOnProperty(name = "course.notifications.enabled", havingValue = "true")` lages bare med `--course.notifications.enabled=true`. Ingen av dem er i `session1-solution`. (c) er ikke testet, men `MiniApp` fra `session1-demo-done` kjører på Boot 4.1.2. Minicontaineren finnes bare i taggen `session1-demo-done`, ikke i `session1-start`, så (c) krever at deltakerne får tilgang til den (for eksempel ved å sjekke ut den taggen).

---

## 🔎 Oppsummering: avvik fra planen og hva som ikke er sjekket

**Avvik fra planen (rett dette i planen):**
1. `/actuator/beans` bruker appnavnet som nøkkel (`company-session1`), ikke `application`.
2. Antall beans er mellom 300 og 400, ikke 150–250.
3. Java 25 og Spring Boot 4.1.2, ikke Java 21 og 3.5.6. Sjekklisten for deltakerne må si JDK 25. Taggene `session1-*` er flyttet til Boot 4.1.2 (bare versjonen i `pom.xml` er endret), og `mvn verify` er grønn på alle tre.
4. Tomcat er 11.0.27 med Boot 4.1.2 (10.1.55 på 3.5.16).

**Målt 8. nov på Boot 4.1.2 med Temurin JDK 25.0.5:** alle oppgavene 1.1–1.5, `@PreDestroy`, `getResourceDescription()`, 1.6 (a) og (b).

**Ikke sjekket:**
- Alt i IntelliJ-GUI-et (jeg kjørte via Maven og `java -jar`).
- Oppgave 1.6 (c).
