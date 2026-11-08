package no.course.company.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import no.course.company.service.OrgNumberValidator;

/**
 * Ikke annotert med @Component: Springs validator-fabrikk oppretter den og
 * injiserer konstruktørargumentet, så beanen fra økt 1 gjenbrukes.
 */
public class OrganizationNumberConstraintValidator implements ConstraintValidator<OrganizationNumber, String> {

    private final OrgNumberValidator orgNumberValidator;

    public OrganizationNumberConstraintValidator(OrgNumberValidator orgNumberValidator) {
        this.orgNumberValidator = orgNumberValidator;
    }

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        return orgNumberValidator.isValid(value);
    }
}
