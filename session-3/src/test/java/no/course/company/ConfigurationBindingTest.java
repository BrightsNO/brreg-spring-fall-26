package no.course.company;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import no.course.company.bean.EmailNotificationSender;
import no.course.company.bean.NotificationSender;
import no.course.company.bean.SmsNotificationSender;
import no.course.company.tasks.RegistryProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.context.ConfigurableApplicationContext;

/** Oppgave 3.2 til 3.5: typede innstillinger per profil, avsender per profil, og verdier som stopper oppstarten. */
class ConfigurationBindingTest {

    private static ConfigurableApplicationContext start(String... args) {
        SpringApplication app = new SpringApplication(CompanyApplication.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        return app.run(args);
    }

    @Test
    void defaultProfileIsLocalWithSmsSender() {
        try (var context = start()) {
            assertThat(context.getEnvironment().getDefaultProfiles()).containsExactly("local");
            RegistryProperties properties = context.getBean(RegistryProperties.class);
            assertThat(properties.maxCompanies()).isEqualTo(5);
            assertThat(properties.name()).isEqualTo("Brights Company Register");
            assertThat(properties.notifications().senderAddress()).isEqualTo("noreply@company.example");
            assertThat(context.getBean(NotificationSender.class)).isInstanceOf(SmsNotificationSender.class);
        }
    }

    @Test
    void testProfileHasItsOwnLimitAndName() {
        try (var context = start("--spring.profiles.active=test")) {
            RegistryProperties properties = context.getBean(RegistryProperties.class);
            assertThat(properties.maxCompanies()).isEqualTo(3);
            assertThat(properties.name()).endsWith("(test)");
        }
    }

    @Test
    void prodWithKeyUsesEmailSender() {
        try (var context = start("--spring.profiles.active=prod", "--registry.notifications.api-key=prod-test-key")) {
            assertThat(context.getBean(RegistryProperties.class).maxCompanies()).isEqualTo(10000);
            assertThat(context.getBean(NotificationSender.class)).isInstanceOf(EmailNotificationSender.class);
        }
    }

    @Test
    void prodWithoutKeyStopsTheStartup() {
        assertThatThrownBy(() -> start("--spring.profiles.active=prod"))
                .hasRootCauseInstanceOf(BindValidationException.class)
                .rootCause()
                .hasMessageContaining("notifications.apiKey")
                .hasMessageContaining("must not be blank");
    }

    @Test
    void invalidLimitStopsTheStartup() {
        assertThatThrownBy(() -> start("--registry.max-companies=0"))
                .hasRootCauseInstanceOf(BindValidationException.class)
                .rootCause()
                .hasMessageContaining("maxCompanies")
                .hasMessageContaining("must be greater than or equal to 1");
    }

    @Test
    void commandLineBeatsTheProfileFile() {
        try (var context = start("--registry.max-companies=2")) {
            assertThat(context.getBean(RegistryProperties.class).maxCompanies()).isEqualTo(2);
        }
    }
}
