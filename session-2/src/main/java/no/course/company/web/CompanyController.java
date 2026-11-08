package no.course.company.web;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;
import no.course.company.domain.Company;
import no.course.company.service.CompanyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

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

    // Oppgave 2.2: 201 med en Location-header og det opprettede selskapet. Oppgave 2.3: @Valid kjører reglene først.
    @PostMapping
    public ResponseEntity<Company> register(@Valid @RequestBody CompanyRequest request) { // <- breakpoint her i demo D1
        Company company = service.register(request.organizationNumber(), request.name());
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(company.id())
                .toUri();
        return ResponseEntity.created(location).body(company);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Company> get(@PathVariable long id) {
        return ResponseEntity.of(service.find(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Company> update(@PathVariable long id, @Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.of(service.update(id, request.organizationNumber(), request.name()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable long id) {
        return service.delete(id)
                ? ResponseEntity.noContent().build()
                : ResponseEntity.notFound().build();
    }
}
