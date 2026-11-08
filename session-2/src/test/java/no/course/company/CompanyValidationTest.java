package no.course.company;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Oppgave 2.3 til 2.5: validering, @OrganizationNumber-constrainten og lesbare feil. */
@SpringBootTest
@AutoConfigureMockMvc
class CompanyValidationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void invalidCompanyGivesProblemDetailWithFieldErrors() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationNumber\":\"12\",\"name\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors.name").value("must not be blank"))
                .andExpect(jsonPath("$.errors.organizationNumber").value("must be exactly 9 digits"));
    }

    @Test
    void emptyBodyGivesErrorsForBothFields() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.organizationNumber").value("must be exactly 9 digits"));
    }

    @Test
    void customConstraintRejectsShortNumberAndAcceptsNineDigits() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationNumber\":\"12\",\"name\":\"Acme\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.organizationNumber").value("must be exactly 9 digits"));

        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationNumber\":\"123456789\",\"name\":\"Acme\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void updateIsValidatedToo() throws Exception {
        mockMvc.perform(put("/api/companies/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationNumber\":\"12\",\"name\":\"Acme\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.organizationNumber").value("must be exactly 9 digits"));
    }

    @Test
    void malformedJsonGivesReadableError() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.detail").value("Malformed JSON request"));
    }
}
