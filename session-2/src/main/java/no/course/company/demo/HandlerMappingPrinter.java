package no.course.company.demo;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

/** Demo D2: oppslagstabellen DispatcherServlet bruker er en bean, og vi kan spørre den. */
@Component
@Profile("demo")
public class HandlerMappingPrinter implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(HandlerMappingPrinter.class);

    private final RequestMappingHandlerMapping mapping;

    public HandlerMappingPrinter(@Qualifier("requestMappingHandlerMapping") RequestMappingHandlerMapping mapping) {
        this.mapping = mapping;
    }

    @Override
    public void run(ApplicationArguments args) {
        mapping.getHandlerMethods().forEach((info, method) -> log.info("Mapping: {} -> {}", info, method));
    }
}
