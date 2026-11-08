package no.course.company;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.test.web.servlet.MockMvc;

/** Registeret er fullt etter tre selskaper. Bruker sin egen applikasjonskontekst, så ingen andre tester påvirkes. */
@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = ClassMode.AFTER_CLASS)
class CompanyLimitTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void fourthCompanyIsRejectedWithConflict() throws Exception {
        for (int i = 1; i <= 3; i++) {
            mockMvc.perform(post("/api/companies").contentType(MediaType.APPLICATION_JSON)
                            .content("{\"organizationNumber\":\"12345678" + i + "\",\"name\":\"Company " + i + "\"}"))
                    .andExpect(status().isCreated());
        }
        mockMvc.perform(post("/api/companies").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationNumber\":\"123456784\",\"name\":\"Company 4\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.detail").value("The register is full: at most 3 companies"));
    }
}
