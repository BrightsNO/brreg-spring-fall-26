package no.course.company;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

/** Ekstraoppgave 2.6: én loggline per forespørsel, også for mislykket validering og 404. */
@SpringBootTest
@AutoConfigureMockMvc
@ExtendWith(OutputCaptureExtension.class)
class RequestTimingFilterTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void logsEveryRequest(CapturedOutput output) throws Exception {
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"organizationNumber\":\"123456789\",\"name\":\"Acme Corp\"}"));
        mockMvc.perform(post("/api/companies")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"organizationNumber\":\"12\",\"name\":\"\"}"));
        mockMvc.perform(get("/api/companies/9999"));

        assertThat(output)
                .containsPattern("POST /api/companies -> 201 in [0-9]+ ms")
                .containsPattern("POST /api/companies -> 400 in [0-9]+ ms")
                .containsPattern("GET /api/companies/9999 -> 404 in [0-9]+ ms");
    }
}
