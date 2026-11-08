package no.course.company.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import no.course.company.bean.NotificationSender;
import no.course.company.domain.Company;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

/** Selskapsregister i minnet. Tjenesten er ferdig: oppgavene i økt 2 handler om weblaget. */
@Service
public class CompanyService {

    private final NotificationSender sender;
    private final Map<Long, Company> companies = new ConcurrentSkipListMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    public CompanyService(@Qualifier("emailNotificationSender") NotificationSender sender) {
        this.sender = sender;
    }

    public Company register(String organizationNumber, String name) {
        Company company = new Company(nextId.getAndIncrement(), organizationNumber, name);
        companies.put(company.id(), company);
        sender.send("Registered company: " + company.name());
        return company;
    }

    public List<Company> all() {
        return new ArrayList<>(companies.values());
    }

    public Optional<Company> find(long id) {
        return Optional.ofNullable(companies.get(id));
    }

    public Optional<Company> update(long id, String organizationNumber, String name) {
        if (!companies.containsKey(id)) {
            return Optional.empty();
        }
        Company updated = new Company(id, organizationNumber, name);
        companies.put(id, updated);
        return Optional.of(updated);
    }

    /** Returnerer true hvis selskapet fantes. */
    public boolean delete(long id) {
        return companies.remove(id) != null;
    }
}
