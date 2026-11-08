package no.course.company.tasks;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import no.course.company.service.OrgNumberValidator;

/**
 * Oppgave 2.4. Ikke en @Component: Springs validator-fabrikk oppretter den og autowirer konstruktøren,
 * så OrgNumberValidator-beanen fra økt 1 blir injisert.
 */
public class OrganizationNumberConstraintValidator implements ConstraintValidator<OrganizationNumber, String> {

    private final OrgNumberValidator validator;

    public OrganizationNumberConstraintValidator(OrgNumberValidator validator) {
        this.validator = validator;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return validator.isValid(value);
    }
}
