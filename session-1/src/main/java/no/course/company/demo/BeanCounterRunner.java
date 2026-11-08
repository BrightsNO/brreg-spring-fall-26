package no.course.company.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/** Skriver ut antall beans i konteksten (demo D2). */
@Component
public class BeanCounterRunner implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(BeanCounterRunner.class);

    private final ApplicationContext ctx;

    public BeanCounterRunner(ApplicationContext ctx) {
        this.ctx = ctx;
    }

    @Override
    public void run(String... args) {
        log.info("Total number of beans: {}", ctx.getBeanDefinitionCount());
    }
}
