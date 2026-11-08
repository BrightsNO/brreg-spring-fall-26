package no.course.company.tasks;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/** Oppgave 1.4: livssyklus. */
@Component
public class LifecycleBean {

    private static final Logger log = LoggerFactory.getLogger(LifecycleBean.class);

    public LifecycleBean() {
        log.info("LifecycleBean: constructor");
    }

    @PostConstruct
    void start() {
        log.info("LifecycleBean: @PostConstruct");
    }

    @PreDestroy
    void stop() {
        log.info("LifecycleBean: @PreDestroy");
    }
}
