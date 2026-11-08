package no.course.company.tasks;

import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

/** Oppgave 1.4: prototype gir en ny instans for hvert injeksjonspunkt. */
@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)
public class PrototypeExample {
}
