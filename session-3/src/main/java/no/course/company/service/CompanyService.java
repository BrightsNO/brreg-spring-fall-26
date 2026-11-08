package no.course.company.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicLong;

import no.course.company.bean.NotificationSender;
import no.course.company.domain.Company;
import no.course.company.tasks.RegistryProperties;
import org.springframework.stereotype.Service;

/** Selskapsregister i minnet. Oppgave 3.2: grensen og navnet kommer fra RegistryProperties. */
@Service
public class CompanyService {

    private final RegistryProperties properties;
    private final NotificationSender sender;
    private final Map<Long, Company> companies = new ConcurrentSkipListMap<>();
    private final AtomicLong nextId = new AtomicLong(1);

    // Oppgave 3.4: den aktive profilen avgjør hvilken NotificationSender som finnes, så @Qualifier trengs ikke
    public CompanyService(RegistryProperties properties, NotificationSender sender) {
        this.properties = properties;
        this.sender = sender;
    }

    public Company register(String organizationNumber, String name) {
        if (companies.size() >= properties.maxCompanies()) {
            throw new CompanyLimitExceededException(properties.maxCompanies());
        }
        Company company = new Company(nextId.getAndIncrement(), organizationNumber, name);
        companies.put(company.id(), company);
        sender.send("[" + properties.name() + "] Registered company: " + company.name());
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
