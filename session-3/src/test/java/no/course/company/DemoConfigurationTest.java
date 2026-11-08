package no.course.company;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import no.course.company.demo.DemoSettings;
import no.course.company.demo.Greeter;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.util.PlaceholderResolutionException;

/** Demo D1–D4: samme app med tre profiler, rekkefølgen på kildene, og verdier som stopper oppstarten. */
class DemoConfigurationTest {

    private static ConfigurableApplicationContext start(String... args) {
        SpringApplication app = new SpringApplication(CompanyApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        return app.run(args);
    }

    @Test
    void eachProfileHasItsOwnGreeterAndRetries() {
        try (var context = start()) {
            assertThat(context.getBean(Greeter.class).greet()).isEqualTo("Hello from the local profile");
            assertThat(context.getBean(DemoSettings.class))
                    .isEqualTo(new DemoSettings("Hello from application-local.yml", 3));
        }
        try (var context = start("--spring.profiles.active=test")) {
            assertThat(context.getBean(Greeter.class).greet()).isEqualTo("Hello from the test profile");
            assertThat(context.getBean(DemoSettings.class))
                    .isEqualTo(new DemoSettings("Hello from application.yml", 1));
        }
        try (var context = start("--spring.profiles.active=prod", "--registry.notifications.api-key=demo-key")) {
            assertThat(context.getBean(Greeter.class).greet()).isEqualTo("Hello from the prod profile");
            assertThat(context.getBean(DemoSettings.class).retries()).isEqualTo(5);
        }
    }

    @Test
    void commandLineBeatsTheProfileFile() {
        try (var context = start("--demo.greeting=from-args")) {
            assertThat(context.getBean(DemoSettings.class).greeting()).isEqualTo("from-args");
            assertThat(context.getEnvironment().getPropertySources().get("commandLineArgs")
                    .getProperty("demo.greeting")).isEqualTo("from-args");
        }
    }

    @Test
    void invalidOrMissingValuesStopTheStartup() {
        assertThatThrownBy(() -> start("--demo.retries=0"))
                .hasRootCauseInstanceOf(BindValidationException.class)
                .rootCause().hasMessageContaining("must be greater than or equal to 1");
        assertThatThrownBy(() -> start("--spring.profiles.active=broken,local"))
                .hasRootCauseInstanceOf(PlaceholderResolutionException.class)
                .rootCause().hasMessageContaining("demo.not-set");
    }
}
