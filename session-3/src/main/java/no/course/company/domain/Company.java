package no.course.company.domain;

/** Et registrert selskap. Bevisst ikke en JPA-entitet: «databasen» er et map i CompanyService. */
public record Company(long id, String organizationNumber, String name) {
}
