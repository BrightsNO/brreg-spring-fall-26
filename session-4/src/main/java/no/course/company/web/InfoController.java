package no.course.company.web;

import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class InfoController {

    /** Skal være åpent for alle. */
    @GetMapping("/public/info")
    public Map<String, String> info() {
        return Map.of("app", "company-session4", "message", "This endpoint is public");
    }

    /** Viser hva SecurityContext inneholder for den aktuelle forespørselen. */
    @GetMapping("/api/me")
    public Map<String, Object> me(Authentication authentication) {
        List<String> authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .sorted()
                .toList();
        return Map.of("name", authentication.getName(), "authorities", authorities);
    }
}
