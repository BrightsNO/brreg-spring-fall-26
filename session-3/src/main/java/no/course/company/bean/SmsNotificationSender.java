package no.course.company.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/** Oppgave 3.4: alle profiler unntatt prod. */
@Component
@Profile("!prod")
public class SmsNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationSender.class);

    @Override
    public void send(String message) {
        log.info("SMS: {}", message);
    }
}
