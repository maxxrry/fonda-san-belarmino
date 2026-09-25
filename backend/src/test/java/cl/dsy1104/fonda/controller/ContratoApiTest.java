package cl.dsy1104.fonda.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Los ejemplos de la seccion 7 del README, por HTTP simulado: pasan por
 * controller, service, repository, H2 y el manejador de errores.
 */
@SpringBootTest
@AutoConfigureMockMvc
class ContratoApiTest {

    @Autowired
    private MockMvc mvc;

    @Test
    void filtrarPorNombreEntregaLasDosChichasConSuPrecio() throws Exception {
        mvc.perform(get("/api/bebidas").param("nombre", "Chicha"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[?(@.tipo == 'ALCOHOLICA')].precio").value(4200))
                .andExpect(jsonPath("$[?(@.tipo == 'SIN_ALCOHOL')].precio").value(2200));
    }

    @Test
    void venderDosPiscoSourResponde201ConLocationYTotal() throws Exception {
        mvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bebidaId\": 2, \"unidades\": 2}"))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", containsString("/api/ventas/")))
                .andExpect(jsonPath("$.nombre").value("Pisco Sour"))
                .andExpect(jsonPath("$.total").value(7000))
                .andExpect(jsonPath("$.estado").value("AUTORIZADA"));
    }

    @Test
    void venderCincoPiscoSourResponde409LimiteExcedido() throws Exception {
        mvc.perform(post("/api/ventas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"bebidaId\": 2, \"unidades\": 5}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("LIMITE_EXCEDIDO"));
    }

    @Test
    void bebidaInvalidaResponde400CampoPorCampo() throws Exception {
        mvc.perform(post("/api/bebidas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"nombre\": \"\", \"volumenML\": 50, \"stock\": 0}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDACION"))
                .andExpect(jsonPath("$.campos.nombre").value("no puede estar vacio"))
                .andExpect(jsonPath("$.campos.volumenML").value("debe estar entre 100 y 3000"));
    }
}
