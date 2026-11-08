package no.course.company.domain;

/** Et selskap. Ligger i minnet: det er ingen database i denne økta. */
public record Company(String organizationNumber, String name) {
}
