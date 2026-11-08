# Økt 1: oppgaver

Du skal bruke verktøyene fra demoene selv, og bygge litt. Hver oppgave har **Mål** (hva), **Hvorfor** (hva du lærer), **Slik gjør du det** og **Ferdig når**.

---

## Før du starter

1. Ha JDK 25, IntelliJ og Maven. (Maven følger med IntelliJ.)
2. Branchen har øktnummer i navnet, slik at den ikke kolliderer med branchene fra de andre øktene:

   ```
   git checkout -b my-answers-1 session1-start
   ```

   **Hvorfor en branch:** du kommer til å endre kode, og vil kunne sammenligne med fasiten uten å miste noe.
3. Åpne mappen `session-1/` (ikke repo-roten) i IntelliJ. Maven-vinduet skal vise `company-session1` (høyreklikk `pom.xml` → *Maven → Sync Project* ved behov).
4. Start `CompanyApplication` (grønn pil). Åpne <http://localhost:8080/actuator/health>. Du skal se `{"status":"UP"}`.

Verktøyene du skal bruke:
- **Debugger**: breakpoint i konstruktøren til `CompanyService`.
- **Actuator**: <http://localhost:8080/actuator/beans>
- **`--debug`**: *Run → Edit Configurations → Program arguments*, skriv `--debug`. Gir en `CONDITIONS EVALUATION REPORT` i konsollen.

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

---

## Oppgave 1.6: Stretch (valgfritt)

**Mål:** velg én eller flere:
- (a) Bytt til Jetty (hvis du ikke gjorde det i demoen).
- (b) Lag en bean som bare opprettes når `course.notifications.enabled=true` (`@ConditionalOnProperty`).
- (c) Utvid minicontaineren med singleton vs. prototype, eller støtte for `@Qualifier`.

**Hvorfor:** (a) beviser at classpath bestemmer. (b) er det samme prinsippet som autokonfigurasjon bruker. (c) viser at du forstår containeren godt nok til å utvide den.
