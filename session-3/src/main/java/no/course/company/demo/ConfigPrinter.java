package no.course.company.demo;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

/** Demo D1–D3: skriver ut hvor demoverdiene havnet etter at alle property-kildene var brukt. */
@Component
public class ConfigPrinter implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(ConfigPrinter.class);

    private final String greeting;
    private final DemoSettings settings;
    private final Greeter greeter;
    private final Environment environment;

    public ConfigPrinter(@Value("${demo.greeting}") String greeting, DemoSettings settings, Greeter greeter,
            Environment environment) {
        this.greeting = greeting;
        this.settings = settings;
        this.greeter = greeter;
        this.environment = environment;
    }

    @Override
    public void run(ApplicationArguments args) {
        log.info("@Value demo.greeting: {}", greeting);
        log.info("Typed settings: {}", settings);
        log.info("Greeter: {}", greeter.greet());
        log.info("Active profiles: {}, default profiles: {}",
                Arrays.toString(environment.getActiveProfiles()), Arrays.toString(environment.getDefaultProfiles()));
    }
}
