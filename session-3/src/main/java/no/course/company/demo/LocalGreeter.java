package no.course.company.demo;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("local")
public class LocalGreeter implements Greeter {

    @Override
    public String greet() {
        return "Hello from the local profile";
    }
}
