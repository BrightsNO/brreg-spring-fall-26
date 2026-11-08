package no.course.company.tasks;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

/**
 * Oppgave 1.2: skriver ut antall beans og navnene på våre egne beans.
 *
 * Oppgave 1.5 (svar): beanen «dataSource» opprettes av
 * DataSourceConfiguration$Hikari (i org.springframework.boot.jdbc.autoconfigure),
 * valgt av DataSourceAutoConfiguration fordi H2 gir en innebygd database,
 * HikariCP ligger på classpath, og vi har ikke definert en egen DataSource-bean.
 * Se: /actuator/beans, --debug-rapporten, eller
 * ((ConfigurableApplicationContext) ctx).getBeanFactory().getBeanDefinition("dataSource").getResourceDescription()
 */
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
        Arrays.stream(ctx.getBeanDefinitionNames())
                .filter(name -> {
                    Class<?> type = ctx.getType(name);
                    return type != null && type.getName().startsWith("no.course");
                })
                .sorted()
                .forEach(name -> log.info("Own bean: {}", name));
    }
}
