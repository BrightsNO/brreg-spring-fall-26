package no.course.company.demo;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/** Demo D4: ber om en verdi ingen setter. Kjør med profilene «broken,local» og les den siste «Caused by». */
@Configuration
@Profile("broken")
public class BrokenConfiguration {

    public BrokenConfiguration(@Value("${demo.not-set}") String notSet) {
    }
}
