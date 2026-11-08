package no.course.company.tasks;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/** Oppgave 1.4: injiserer prototype og singleton to steder og sammenligner med ==. */
@Component
public class ScopeComparer implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(ScopeComparer.class);

    private final PrototypeExample prototype1;
    private final PrototypeExample prototype2;
    private final SingletonExample singleton1;
    private final SingletonExample singleton2;

    public ScopeComparer(PrototypeExample prototype1, PrototypeExample prototype2,
                         SingletonExample singleton1, SingletonExample singleton2) {
        this.prototype1 = prototype1;
        this.prototype2 = prototype2;
        this.singleton1 = singleton1;
        this.singleton2 = singleton2;
    }

    @Override
    public void run(String... args) {
        log.info("prototype1 == prototype2: {}", prototype1 == prototype2); // false
        log.info("singleton1 == singleton2: {}", singleton1 == singleton2); // true
    }
}
