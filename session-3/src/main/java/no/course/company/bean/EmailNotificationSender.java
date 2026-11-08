package no.course.company.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(EmailNotificationSender.class);

    // TODO 3.2: avsenderadressen skal komme fra konfigurasjon
    private static final String FROM = "noreply@company.example";

    @Override
    public void send(String message) {
        log.info("Email from {}: {}", FROM, message);
    }
}
