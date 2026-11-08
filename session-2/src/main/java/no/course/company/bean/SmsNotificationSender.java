package no.course.company.bean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * En andre NotificationSender. CompanyService velger e-postvarianten eksplisitt med @Qualifier,
 * akkurat som i fasiten til økt 1.
 */
@Component
public class SmsNotificationSender implements NotificationSender {

    private static final Logger log = LoggerFactory.getLogger(SmsNotificationSender.class);

    @Override
    public void send(String message) {
        log.info("SMS: {}", message);
    }
}
