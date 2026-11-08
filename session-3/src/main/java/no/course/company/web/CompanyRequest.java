package no.course.company.web;

import jakarta.validation.constraints.NotBlank;
import no.course.company.validation.OrganizationNumber;

/** JSON-body for POST og PUT. Id-en hentes aldri fra klienten. */
public record CompanyRequest(@OrganizationNumber String organizationNumber, @NotBlank String name) {
}
