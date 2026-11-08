package no.course.company.tasks;

import no.course.company.bean.NotificationSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Oppgave 1.3: nå en @Component. Da finnes det to NotificationSender-kandidater, og
 * CompanyService velger eksplisitt med @Qualifier("emailNotificationSender").
 * (Alternativ: @Primary på en av dem.)
 */
@Component
public class SmsNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationSender.class);

    @Override
    public void send(String message) {
        log.info("SMS: {}", message);
    }
}
