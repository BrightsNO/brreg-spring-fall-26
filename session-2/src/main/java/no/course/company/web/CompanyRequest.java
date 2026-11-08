package no.course.company.web;

import jakarta.validation.constraints.NotBlank;
import no.course.company.tasks.OrganizationNumber;

/**
 * JSON-body for POST og PUT. Id-en hentes aldri fra klienten.
 * Oppgave 2.3: Bean Validation-annotasjoner. Oppgave 2.4: @OrganizationNumber erstatter @Pattern.
 */
public record CompanyRequest(@OrganizationNumber String organizationNumber, @NotBlank String name) {
}
