package no.course.company.web;

import java.util.List;

import no.course.company.domain.Company;
import no.course.company.service.CompanyService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/companies")
public class CompanyController {

    private final CompanyService service;

    public CompanyController(CompanyService service) {
        this.service = service;
    }

    @GetMapping
    public List<Company> all() {
        return service.all();
    }

    // Ingen validering ennå (det kommer i økt 2)
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@RequestBody Company company) {
        service.register(company);
    }
}
