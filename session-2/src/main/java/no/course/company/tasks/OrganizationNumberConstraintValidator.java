package no.course.company.tasks;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

/**
 * Oppgave 2.4.
 * TODO: ta OrgNumberValidator som konstruktørparameter og bruk den i isValid.
 * IKKE annoter denne klassen med @Component: Springs validator-fabrikk oppretter den.
 */
public class OrganizationNumberConstraintValidator implements ConstraintValidator<OrganizationNumber, String> {

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return true; // TODO
    }
}
