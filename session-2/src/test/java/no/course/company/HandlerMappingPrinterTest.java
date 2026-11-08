package no.course.company;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.test.context.ActiveProfiles;

/** Demo D2: med profilen «demo» skrives handler-mappingene ut ved oppstart. */
@SpringBootTest
@ActiveProfiles("demo")
@ExtendWith(OutputCaptureExtension.class)
class HandlerMappingPrinterTest {

    @Test
    void mappingsArePrinted(CapturedOutput output) {
        assertThat(output).contains("Mapping: {POST [/api/companies]} -> no.course.company.web.CompanyController#register");
    }
}
