package no.course.company.demo;

import jakarta.servlet.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

/**
 * Demo D2 (valgfri): skriver ut filterkjeden ved oppstart. Dette er det samme deltakerne
 * skriver i oppgave 4.1, så den er bare aktiv med profilen «print-filters» for å holde startstate
 * til oppgavene fri for den.
 */
@Component
@Profile("print-filters")
public class FilterChainPrinter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FilterChainPrinter.class);

    private final FilterChainProxy filterChainProxy;

    public FilterChainPrinter(FilterChainProxy filterChainProxy) {
        this.filterChainProxy = filterChainProxy;
    }

    @Override
    public void run(String... args) {
        for (SecurityFilterChain chain : filterChainProxy.getFilterChains()) {
            log.info("Security filter chain with {} filters", chain.getFilters().size());
            int position = 0;
            for (Filter filter : chain.getFilters()) {
                log.info("  {}. {}", ++position, filter.getClass().getSimpleName());
            }
        }
    }
}
