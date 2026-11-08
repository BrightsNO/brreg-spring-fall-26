package no.course.company.tasks;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Oppgave 4.6: logger metode, sti, bruker og status for hver forespørsel.
 *
 * Bevisst IKKE en @Component: da ville Spring Boot også registrert det som et vanlig servlet-
 * filter, og det ville kjørt to ganger. Vi oppretter det selv i SecurityConfig.
 */
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String user = authentication == null ? "anonymous" : authentication.getName();
        try {
            filterChain.doFilter(request, response);
        } finally {
            log.info("{} {} user={} status={}", request.getMethod(), request.getRequestURI(), user, response.getStatus());
        }
    }
}
