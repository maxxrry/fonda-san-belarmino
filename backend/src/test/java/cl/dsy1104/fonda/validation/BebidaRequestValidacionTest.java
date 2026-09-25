package cl.dsy1104.fonda.validation;

import cl.dsy1104.fonda.dto.BebidaRequest;
import cl.dsy1104.fonda.model.TipoBebida;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Tabla de la seccion 4 del README para gradosAlcohol y azucarPorLitro. */
class BebidaRequestValidacionTest {

    // El mismo motor que usa @Valid, sin levantar Spring.
    private final Validator validator = Validation.buildDefaultValidatorFactory().getValidator();

    @Test
    void alcoholicaCompletaEsValida() {
        assertTrue(errores(alcoholica(18.0, null)).isEmpty());
    }

    @Test
    void sinAlcoholCompletaEsValida() {
        assertTrue(errores(sinAlcohol(null, 95)).isEmpty());
    }

    @Test
    void alcoholicaSinGradosFallaEnGradosAlcohol() {
        assertEquals(Map.of("gradosAlcohol", "es obligatorio en bebidas alcoholicas"),
                errores(alcoholica(null, null)));
    }

    @Test
    void alcoholicaConAzucarFallaEnAzucarPorLitro() {
        assertEquals(Map.of("azucarPorLitro", "debe quedar vacio en bebidas alcoholicas"),
                errores(alcoholica(18.0, 50)));
    }

    @Test
    void gradosFueraDeRangoFallan() {
        assertEquals(Map.of("gradosAlcohol", "debe estar entre 0.5 y 45"), errores(alcoholica(0.4, null)));
        assertEquals(Map.of("gradosAlcohol", "debe estar entre 0.5 y 45"), errores(alcoholica(45.1, null)));
        assertTrue(errores(alcoholica(0.5, null)).isEmpty());
        assertTrue(errores(alcoholica(45.0, null)).isEmpty());
    }

    @Test
    void sinAlcoholSinAzucarFallaEnAzucarPorLitro() {
        assertEquals(Map.of("azucarPorLitro", "es obligatorio en bebidas sin alcohol"),
                errores(sinAlcohol(null, null)));
    }

    @Test
    void sinAlcoholConGradosFallaEnGradosAlcohol() {
        assertEquals(Map.of("gradosAlcohol", "debe quedar vacio en bebidas sin alcohol"),
                errores(sinAlcohol(12.0, 95)));
    }

    @Test
    void azucarNegativaFalla() {
        assertEquals(Map.of("azucarPorLitro", "debe ser mayor o igual a cero"),
                errores(sinAlcohol(null, -1)));
    }

    private BebidaRequest alcoholica(Double grados, Integer azucar) {
        return new BebidaRequest("Pisco Sour", TipoBebida.ALCOHOLICA, 500, 10, grados, true, azucar);
    }

    private BebidaRequest sinAlcohol(Double grados, Integer azucar) {
        return new BebidaRequest("Mote", TipoBebida.SIN_ALCOHOL, 400, 10, grados, null, azucar);
    }

    // campo -> mensaje, igual a lo que termina en "campos" del 400.
    private Map<String, String> errores(BebidaRequest request) {
        return validator.validate(request).stream()
                .collect(Collectors.toMap(
                        v -> v.getPropertyPath().toString(),
                        ConstraintViolation::getMessage));
    }
}
