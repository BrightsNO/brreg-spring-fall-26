package no.course.company;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasItem;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Kjører de samme sjekkene som Postman-samlingen, uten å starte en server. */
@SpringBootTest
@AutoConfigureMockMvc
class SecurityRulesTest {

    private static final String NEW_COMPANY = "{\"organizationNumber\":\"555666777\",\"name\":\"Test AS\"}";

    @Autowired
    MockMvc mvc;

    @Test
    void publicEndpointsAreOpen() throws Exception {
        mvc.perform(get("/public/info")).andExpect(status().isOk());
        mvc.perform(get("/actuator/health")).andExpect(status().isOk());
    }

    @Test
    void noLoginGives401() throws Exception {
        mvc.perform(get("/api/companies")).andExpect(status().isUnauthorized());
    }

    @Test
    void wrongPasswordGives401() throws Exception {
        mvc.perform(get("/api/companies").with(httpBasic("user", "wrong")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void userCanRead() throws Exception {
        mvc.perform(get("/api/companies").with(httpBasic("user", "password")))
                .andExpect(status().isOk());
        mvc.perform(get("/api/me").with(httpBasic("user", "password")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("user"))
                .andExpect(jsonPath("$.authorities", hasItem("ROLE_USER")));
    }

    @Test
    void userCannotWrite() throws Exception {
        mvc.perform(post("/api/companies").with(httpBasic("user", "password"))
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_COMPANY))
                .andExpect(status().isForbidden());
        mvc.perform(delete("/api/companies/123456789").with(httpBasic("user", "password")))
                .andExpect(status().isForbidden());
    }

    @Test
    void adminCanWriteWithoutCsrfToken() throws Exception {
        mvc.perform(post("/api/companies").with(httpBasic("admin", "admin"))
                        .contentType(MediaType.APPLICATION_JSON).content(NEW_COMPANY))
                .andExpect(status().isCreated());
        mvc.perform(delete("/api/companies/555666777").with(httpBasic("admin", "admin")))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(roles = "USER")
    void mockUserCanRead() throws Exception {
        mvc.perform(get("/api/companies")).andExpect(status().isOk());
    }

    @Test
    void preflightFromAllowedOriginIsAccepted() throws Exception {
        mvc.perform(options("/api/companies")
                        .header("Origin", "http://localhost:3000")
                        .header("Access-Control-Request-Method", "POST")
                        .header("Access-Control-Request-Headers", "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", "http://localhost:3000"));
    }

    @Test
    void preflightFromOtherOriginIsRejected() throws Exception {
        mvc.perform(options("/api/companies")
                        .header("Origin", "http://evil.example")
                        .header("Access-Control-Request-Method", "POST"))
                .andExpect(status().isForbidden());
    }
}
