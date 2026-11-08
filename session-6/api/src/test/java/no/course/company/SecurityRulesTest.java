package no.course.company;

import org.junit.jupiter.api.Test;

/**
 * Oppgave 6.5: test reglene uten autorisasjonsserver, med jwt()-post-processoren.
 *
 * TODO: gjør dette til en @SpringBootTest med @AutoConfigureMockMvc, injiser MockMvc, og skriv én test per rad i
 * tabellen fra oppgave 6.4. Bruk  .with(jwt().authorities(new SimpleGrantedAuthority("SCOPE_companies.read")))
 * fra SecurityMockMvcRequestPostProcessors.
 *
 * Merk: denne klassen går bare grønt når oppgave 6.3 er gjort, fordi testkonteksten trenger JwtDecoder fra issuer-uri.
 */
class SecurityRulesTest {

    @Test
    void todo() {
        // erstatt dette med testene dine
    }
}
