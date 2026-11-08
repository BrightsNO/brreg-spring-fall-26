package no.course.company.validation;

import static java.lang.annotation.ElementType.FIELD;
import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.ElementType.RECORD_COMPONENT;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

/** En constraint for organisasjonsnumre (økt 2), sjekket av OrganizationNumberConstraintValidator. */
@Target({FIELD, PARAMETER, RECORD_COMPONENT})
@Retention(RUNTIME)
@Constraint(validatedBy = OrganizationNumberConstraintValidator.class)
public @interface OrganizationNumber {

    String message() default "must be exactly 9 digits";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}
