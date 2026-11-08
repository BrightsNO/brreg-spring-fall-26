package no.course.company.tasks;

import java.io.IOException;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

/**
 * Ekstraoppgave: logg metode, sti, status og varighet i ms for hver forespørsel.
 * TODO: gjør det til en bean (@Component), mål med System.nanoTime(), logg etter chain.doFilter.
 */
public class RequestTimingFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        chain.doFilter(request, response); // TODO
    }
}
