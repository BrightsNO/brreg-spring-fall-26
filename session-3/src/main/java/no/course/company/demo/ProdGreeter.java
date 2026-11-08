package no.course.company.demo;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class ProdGreeter implements Greeter {

    @Override
    public String greet() {
        return "Hello from the prod profile";
    }
}
