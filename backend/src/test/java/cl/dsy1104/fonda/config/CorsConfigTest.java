package cl.dsy1104.fonda.config;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/** Simula la consulta previa (preflight) que hace el navegador antes de un PATCH. */
@SpringBootTest
@AutoConfigureMockMvc
class CorsConfigTest {

    @Autowired
    private MockMvc mvc;

    @Value("${fonda.cors.origen}")
    private String origenFrontend;

    @Test
    void elOrigenDelFrontendQuedaAutorizado() throws Exception {
        mvc.perform(options("/api/bebidas/1/restriccion")
                        .header("Origin", origenFrontend)
                        .header("Access-Control-Request-Method", "PATCH"))
                .andExpect(status().isOk())
                .andExpect(header().string("Access-Control-Allow-Origin", origenFrontend));
    }

    @Test
    void otroOrigenQuedaRechazado() throws Exception {
        mvc.perform(options("/api/bebidas")
                        .header("Origin", "http://sitio-ajeno.com")
                        .header("Access-Control-Request-Method", "GET"))
                .andExpect(status().isForbidden());
    }
}
