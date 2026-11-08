package no.course.company.tasks;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/**
 * Oppgave 3.2: de typede, validerte innstillingene til registeret, bundet fra «registry.*».
 * Oppgave 3.5: notifications.apiKey må komme fra miljøet i prod, aldri fra repoet.
 */
@ConfigurationProperties(prefix = "registry")
@Validated
public record RegistryProperties(
        @NotBlank String name,
        @Min(1) @Max(10000) int maxCompanies,
        @Valid @NotNull Notifications notifications) {

    public record Notifications(@NotBlank String senderAddress, @NotBlank String apiKey) {
    }
}
