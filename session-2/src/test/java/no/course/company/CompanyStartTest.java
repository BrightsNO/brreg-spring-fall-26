package no.course.company;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Hva startprosjektet gjør før noen oppgave er løst. */
@SpringBootTest
@AutoConfigureMockMvc
class CompanyStartTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void listIsAnArrayWithVersionHeader() throws Exception {
        mockMvc.perform(get("/api/companies"))
                .andExpect(status().isOk())
                .andExpect(header().string("X-Api-Version", "1"))
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    void postGivesEmptyCreated() throws Exception {
        mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationNumber\":\"123456789\",\"name\":\"Acme Corp\"}"))
                .andExpect(status().isCreated())
                .andExpect(content().string(""));
    }
}
