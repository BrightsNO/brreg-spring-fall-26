package no.course.company.service;

import org.springframework.stereotype.Component;

/** Økt 1, oppgave 1.2: enkel sjekk av et organisasjonsnummer (9 siffer). */
@Component
public class OrgNumberValidator {

    public boolean isValid(String organizationNumber) {
        return organizationNumber != null && organizationNumber.matches("[0-9]{9}");
    }
}
