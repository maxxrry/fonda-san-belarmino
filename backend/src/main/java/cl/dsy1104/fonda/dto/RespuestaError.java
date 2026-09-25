package cl.dsy1104.fonda.dto;

/** Cuerpo de los errores 404, 409 y demas: { "error": "<CODIGO>", "mensaje": "<texto>" }. */
public record RespuestaError(String error, String mensaje) {
}
