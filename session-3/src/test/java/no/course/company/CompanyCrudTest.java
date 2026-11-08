package no.course.company;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
class CompanyCrudTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void fullLifecycle() throws Exception {
        var created = mockMvc.perform(post("/api/companies")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationNumber\":\"123456789\",\"name\":\"Acme Corp\"}"))
                .andExpect(status().isCreated())
                .andExpect(header().exists("Location"))
                .andExpect(jsonPath("$.name").value("Acme Corp"))
                .andReturn();

        long id = objectMapper.readTree(created.getResponse().getContentAsString()).get("id").asLong();
        String location = created.getResponse().getHeader("Location");
        assertThat(location).endsWith("/api/companies/" + id);

        mockMvc.perform(get("/api/companies/" + id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationNumber").value("123456789"));

        mockMvc.perform(put("/api/companies/" + id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationNumber\":\"987654321\",\"name\":\"Acme Corp AS\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Acme Corp AS"));

        mockMvc.perform(delete("/api/companies/" + id)).andExpect(status().isNoContent());
        mockMvc.perform(get("/api/companies/" + id)).andExpect(status().isNotFound());
    }

    @Test
    void unknownIdGives404ForGetPutAndDelete() throws Exception {
        mockMvc.perform(get("/api/companies/9999")).andExpect(status().isNotFound());
        mockMvc.perform(put("/api/companies/9999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"organizationNumber\":\"123456789\",\"name\":\"Nobody\"}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(delete("/api/companies/9999")).andExpect(status().isNotFound());
    }
}
