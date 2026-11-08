package no.course.company.minicontainer.demo;

import no.course.company.minicontainer.MiniComponent;

@MiniComponent
public class EmailNotificationSender implements NotificationSender {
    public EmailNotificationSender() { }

    @Override
    public void send(String message) {
        System.out.println("Email: " + message);
    }
}
