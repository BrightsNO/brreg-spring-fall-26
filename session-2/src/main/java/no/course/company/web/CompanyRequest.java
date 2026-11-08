package no.course.company.web;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/**
 * JSON-body for POST og PUT. Id-en hentes aldri fra klienten.
 * Demo D3: valideringsreglene er annotasjoner på dataene; @Valid i controlleren kjører dem.
 */
public record CompanyRequest(
        @Pattern(regexp = "[0-9]{9}", message = "must be exactly 9 digits") String organizationNumber,
        @NotBlank String name) {
}
