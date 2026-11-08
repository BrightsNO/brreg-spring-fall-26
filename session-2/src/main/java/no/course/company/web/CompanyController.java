package no.course.company.web;

import java.util.List;

import jakarta.validation.Valid;
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

    // Demo D3: fjern @Valid, start på nytt og POST ugyldige data: breakpointet treffes. Sett den tilbake: 400, ingen breakpoint.
    // TODO 2.2: returner 201 med en Location-header og det opprettede selskapet i stedet for tom body
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public void register(@Valid @RequestBody CompanyRequest request) { // <- breakpoint her i demo D1
        service.register(request.organizationNumber(), request.name());
    }

    // TODO 2.2: GET /{id}: 200 med selskapet, eller 404
    // TODO 2.2: PUT /{id}: 200 med det oppdaterte selskapet, eller 404
    // TODO 2.2: DELETE /{id}: 204, eller 404
}
