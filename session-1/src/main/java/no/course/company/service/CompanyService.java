package no.course.company.service;

import java.util.ArrayList;
import java.util.List;

import no.course.company.bean.NotificationSender;
import no.course.company.domain.Company;
import no.course.company.tasks.OrgNumberValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);

    private final NotificationSender sender;
    private final OrgNumberValidator validator;
    private final List<Company> companies = new ArrayList<>();

    public CompanyService(@Qualifier("emailNotificationSender") NotificationSender sender,
                          OrgNumberValidator validator) {
        log.info("CompanyService is being created"); // <- breakpoint her i demo D2
        this.sender = sender;
        this.validator = validator;
    }

    public void register(Company company) {
        if (!validator.isValid(company.organizationNumber())) {
            log.warn("Invalid organization number: {}", company.organizationNumber());
        }
        companies.add(company);
        sender.send("Registered company: " + company.name());
    }

    public List<Company> all() {
        return new ArrayList<>(companies);
    }
}
