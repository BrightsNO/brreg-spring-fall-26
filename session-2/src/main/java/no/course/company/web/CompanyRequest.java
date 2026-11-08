package no.course.company.web;

/**
 * JSON-body for POST og PUT. Id-en hentes aldri fra klienten.
 * TODO 2.3: legg til Bean Validation-annotasjoner (navn ikke tomt, organisasjonsnummer 9 siffer).
 */
public record CompanyRequest(String organizationNumber, String name) {
}
