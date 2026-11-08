package no.course.company;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.test.web.servlet.MockMvc;

/** Oppgave 2.2: hele CRUD-forløpet med riktige statuskoder og headere. */
@SpringBootTest
@AutoConfigureMockMvc
class CompanyCrudTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void createReadUpdateDelete() throws Exception {
        String body = mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationNumber\":\"123456789\",\"name\":\"Acme Corp\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Acme Corp"))
                .andReturn().getResponse().getContentAsString();
        long id = ((Number) JsonPath.read(body, "$.id")).longValue();

        mockMvc.perform(get("/api/companies/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationNumber").value("123456789"));

        mockMvc.perform(put("/api/companies/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationNumber\":\"987654321\",\"name\":\"Acme Corp AS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.name").value("Acme Corp AS"));

        mockMvc.perform(delete("/api/companies/" + id))
                .andExpect(status().isNoContent())
                .andExpect(content().string(""));

        mockMvc.perform(get("/api/companies/" + id)).andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/companies/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void createSetsLocationHeaderToTheNewCompany() throws Exception {
        MockHttpServletResponse response = mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationNumber\":\"111222333\",\"name\":\"Location Corp\"}"))
                .andExpect(status().isCreated())
                .andReturn().getResponse();
        long id = ((Number) JsonPath.read(response.getContentAsString(), "$.id")).longValue();

        assertThat(response.getHeader("Location")).endsWith("/api/companies/" + id);
    }

    @Test
    void unknownIdGivesNotFound() throws Exception {
        mockMvc.perform(get("/api/companies/9999")).andExpect(status().isNotFound());
        mockMvc.perform(put("/api/companies/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationNumber\":\"123456789\",\"name\":\"Nobody\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/companies/9999")).andExpect(status().isNotFound());
    }
}
