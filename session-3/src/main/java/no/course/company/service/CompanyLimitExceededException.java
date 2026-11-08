package no.course.company.service;

/** Kastes når registeret er fullt. Weblaget gjør det om til 409 Conflict. */
public class CompanyLimitExceededException extends RuntimeException {

    public CompanyLimitExceededException(int limit) {
        super("The register is full: at most " + limit + " companies");
    }
}
