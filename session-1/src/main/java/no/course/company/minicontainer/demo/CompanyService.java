package no.course.company.minicontainer.demo;

import no.course.company.minicontainer.MiniComponent;

@MiniComponent
public class CompanyService {
    private final NotificationSender sender;

    public CompanyService(NotificationSender sender) {
        this.sender = sender;
    }

    public void register(String name) {
        sender.send("Registered company: " + name);
    }
}
