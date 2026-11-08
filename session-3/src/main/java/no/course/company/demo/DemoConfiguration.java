package no.course.company.demo;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/** Registrerer DemoSettings som en bean. Oppgavene bruker @ConfigurationPropertiesScan i stedet (oppgave 3.2). */
@Configuration
@EnableConfigurationProperties(DemoSettings.class)
public class DemoConfiguration {
}
