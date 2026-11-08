package no.course.company.service;

import java.util.ArrayList;
import java.util.List;

import no.course.company.bean.NotificationSender;
import no.course.company.domain.Company;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    private static final Logger log = LoggerFactory.getLogger(CompanyService.class);

    private final NotificationSender sender;
    private final List<Company> companies = new ArrayList<>();

    public CompanyService(NotificationSender sender) {
        log.info("CompanyService is being created"); // <- breakpoint her i demo D2
        this.sender = sender;
    }

    public void register(Company company) {
        companies.add(company);
        sender.send("Registered company: " + company.name());
    }

    public List<Company> all() {
        return new ArrayList<>(companies);
    }
}
