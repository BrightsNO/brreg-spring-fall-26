package no.course.company.tasks;

/**
 * Oppgave 3.2: de typede, validerte innstillingene til registeret.
 * TODO: annoter med @ConfigurationProperties(prefix = "registry") og @Validated, og legg deretter til:
 *   - name (ikke tom)
 *   - maxCompanies (1 til 10000)
 *   - en nøstet record «notifications» med senderAddress (ikke tom)
 * Husk å registrere den (for eksempel med @ConfigurationPropertiesScan på CompanyApplication).
 * Oppgave 3.5 legger til ett felt til i den nøstede recorden.
 */
public record RegistryProperties() {
}
