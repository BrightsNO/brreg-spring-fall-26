package no.course.company.tasks;

import org.springframework.stereotype.Component;

/** Oppgave 1.2: enkel sjekk av et organisasjonsnummer (9 siffer). */
@Component
public class OrgNumberValidator {

    public boolean isValid(String organizationNumber) {
        return organizationNumber != null && organizationNumber.matches("[0-9]{9}");
    }
}
