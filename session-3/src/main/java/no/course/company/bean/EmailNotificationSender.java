package no.course.company.bean;

import no.course.company.tasks.RegistryProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Oppgave 3.4: bare i prod. Oppgave 3.2: avsenderadressen kommer fra konfigurasjon. */
@Component
@Profile("prod")
public class EmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    private final RegistryProperties properties;

    public EmailNotificationSender(RegistryProperties properties) {
        this.properties = properties;
    }

    @Override
    public void send(String message) {
        log.info("Email from {}: {}", properties.notifications().senderAddress(), message);
    }
}
