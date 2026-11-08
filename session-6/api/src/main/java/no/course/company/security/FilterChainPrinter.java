package no.course.company.security;

import jakarta.servlet.Filter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.web.FilterChainProxy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.stereotype.Component;

/** Skriver ut filterkjeden til Spring Security ved oppstart. */
@Component
public class FilterChainPrinter implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(FilterChainPrinter.class);

    private final FilterChainProxy filterChainProxy;

    public FilterChainPrinter(FilterChainProxy filterChainProxy) {
        this.filterChainProxy = filterChainProxy;
    }

    @Override
    public void run(String... args) {
        int chainNumber = 0;
        for (SecurityFilterChain chain : filterChainProxy.getFilterChains()) {
            chainNumber++;
            log.info("Security filter chain {}: {} filters", chainNumber, chain.getFilters().size());
            int position = 0;
            for (Filter filter : chain.getFilters()) {
                position++;
                log.info("  {}. {}", position, filter.getClass().getSimpleName());
            }
        }
    }
}
