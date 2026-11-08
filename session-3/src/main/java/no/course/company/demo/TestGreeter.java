package no.course.company.demo;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("test")
public class TestGreeter implements Greeter {

    @Override
    public String greet() {
        return "Hello from the test profile";
    }
}
