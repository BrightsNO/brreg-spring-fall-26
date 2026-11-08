package no.course.company.demo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

/** Demo D2: typede, validerte innstillinger under prefikset «demo». Prøv --demo.retries=0. */
@ConfigurationProperties(prefix = "demo")
@Validated
public record DemoSettings(@NotBlank String greeting, @Min(1) int retries) {
}
