package cl.dsy1104.fonda.dto;

import java.util.Map;

/** Cuerpo del 400: { "error": "VALIDACION", "campos": { "<campo>": "<mensaje>" } }. */
public record RespuestaErrorValidacion(String error, Map<String, String> campos) {

    public RespuestaErrorValidacion(Map<String, String> campos) {
        this("VALIDACION", campos);
    }
}
