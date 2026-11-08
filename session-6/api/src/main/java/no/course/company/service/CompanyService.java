package no.course.company.service;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

import no.course.company.domain.Company;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

@Service
public class CompanyService {

    private final Map<String, Company> companies = new ConcurrentHashMap<>();

    public CompanyService() {
        register(new Company("123456789", "Acme Corp"));
        register(new Company("987654321", "Bright Test AS"));
    }

    public List<Company> all() {
        return companies.values().stream()
                .sorted(Comparator.comparing(Company::organizationNumber))
                .toList();
    }

    public Optional<Company> find(String organizationNumber) {
        return Optional.ofNullable(companies.get(organizationNumber));
    }

    /** Rollen sjekkes på metoden (økt 5, oppgave 5.6). */
    @PreAuthorize("hasRole('ADMIN')")
    public void register(Company company) {
        companies.put(company.organizationNumber(), company);
    }

    /** Rollen sjekkes på metoden (økt 5, oppgave 5.6). */
    @PreAuthorize("hasRole('ADMIN')")
    public boolean delete(String organizationNumber) {
        return companies.remove(organizationNumber) != null;
    }
}
