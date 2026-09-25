package cl.dsy1104.fonda.exception;

import cl.dsy1104.fonda.dto.RespuestaError;
import cl.dsy1104.fonda.dto.RespuestaErrorValidacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Unico lugar donde una excepcion se convierte en respuesta HTTP de error.
 * Los controllers solo lanzan (o dejan pasar) excepciones; nunca arman errores.
 *
 * Extiende ResponseEntityExceptionHandler: esa clase de Spring ya sabe que
 * codigo le corresponde a cada error de Spring MVC (415, 405, JSON mal
 * formado...). Aqui solo se cambia el cuerpo para que siga el contrato.
 */
@RestControllerAdvice
public class ManejadorGlobalErrores extends ResponseEntityExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(ManejadorGlobalErrores.class);

    // --- 404 ---------------------------------------------------------------

    @ExceptionHandler(BebidaNoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public RespuestaError bebidaNoEncontrada(BebidaNoEncontradaException ex) {
        return new RespuestaError("BEBIDA_NO_ENCONTRADA", ex.getMessage());
    }

    @ExceptionHandler(VentaNoEncontradaException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public RespuestaError ventaNoEncontrada(VentaNoEncontradaException ex) {
        return new RespuestaError("VENTA_NO_ENCONTRADA", ex.getMessage());
    }

    // --- 409 ---------------------------------------------------------------

    // El codigo es el motivo: VENTA_RESTRINGIDA, LIMITE_EXCEDIDO o STOCK_INSUFICIENTE.
    @ExceptionHandler(VentaRechazadaException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError ventaRechazada(VentaRechazadaException ex) {
        return new RespuestaError(ex.getMotivo().name(), ex.getMessage());
    }

    @ExceptionHandler(BebidaConVentasException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public RespuestaError bebidaConVentas(BebidaConVentasException ex) {
        return new RespuestaError("BEBIDA_CON_VENTAS", ex.getMessage());
    }

    // --- 400: fallo @Valid --------------------------------------------------

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(
            MethodArgumentNotValidException ex, HttpHeaders headers,
            HttpStatusCode status, WebRequest request) {
        // LinkedHashMap mantiene el orden; putIfAbsent deja un mensaje por campo.
        Map<String, String> campos = new LinkedHashMap<>();
        for (FieldError error : ex.getBindingResult().getFieldErrors()) {
            campos.putIfAbsent(error.getField(), error.getDefaultMessage());
        }
        return ResponseEntity.badRequest().body(new RespuestaErrorValidacion(campos));
    }

    // --- Resto de errores de Spring MVC: se respeta su codigo --------------

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(
            Exception ex, Object body, HttpHeaders headers,
            HttpStatusCode statusCode, WebRequest request) {
        RespuestaError error = switch (statusCode.value()) {
            case 400 -> new RespuestaError("SOLICITUD_INVALIDA",
                    "La solicitud tiene un formato invalido.");
            case 404 -> new RespuestaError("RUTA_NO_ENCONTRADA",
                    "La ruta pedida no existe.");
            case 405 -> new RespuestaError("METODO_NO_PERMITIDO",
                    "Esta ruta no admite ese metodo HTTP.");
            case 415 -> new RespuestaError("TIPO_CONTENIDO_NO_SOPORTADO",
                    "El cuerpo debe enviarse como JSON (Content-Type: application/json).");
            default -> new RespuestaError("ERROR_HTTP",
                    "No se pudo procesar la solicitud.");
        };
        return ResponseEntity.status(statusCode).headers(headers).body(error);
    }

    // --- 500: cualquier otra cosa -------------------------------------------

    // La traza va al log del servidor, no al cliente.
    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    public RespuestaError errorInesperado(Exception ex) {
        log.error("Error no controlado", ex);
        return new RespuestaError("ERROR_INTERNO", "Ocurrio un error inesperado.");
    }
}
