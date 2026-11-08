package no.course.company.tasks;

import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Oppgave 2.5: gjør MethodArgumentNotValidException om til en ProblemDetail med status 400 og en
 * «errors»-egenskap som mapper feltnavn til melding.
 * TODO: legg til en @ExceptionHandler-metode.
 */
@RestControllerAdvice
public class ApiExceptionHandler {
}
