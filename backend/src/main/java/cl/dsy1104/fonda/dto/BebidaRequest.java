package cl.dsy1104.fonda.dto;

import cl.dsy1104.fonda.model.TipoBebida;
import cl.dsy1104.fonda.validation.AtributosSegunTipo;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos que llegan en POST y PUT de /api/bebidas. El controller lo valida con
 * @Valid antes de pasarlo al service.
 *
 * Las anotaciones de campo revisan cada valor por separado (e ignoran null).
 * @AtributosSegunTipo revisa que gradosAlcohol y azucarPorLitro vengan o no
 * segun el tipo.
 */
@AtributosSegunTipo
public record BebidaRequest(

        @NotBlank(message = "no puede estar vacio")
        String nombre,

        @NotNull(message = "es obligatorio")
        TipoBebida tipo,

        @NotNull(message = "es obligatorio")
        @Min(value = 100, message = "debe estar entre 100 y 3000")
        @Max(value = 3000, message = "debe estar entre 100 y 3000")
        Integer volumenML,

        @NotNull(message = "es obligatorio")
        @Min(value = 0, message = "debe ser mayor o igual a cero")
        Integer stock,

        @DecimalMin(value = "0.5", message = "debe estar entre 0.5 y 45")
        @DecimalMax(value = "45", message = "debe estar entre 0.5 y 45")
        Double gradosAlcohol,

        Boolean certificada,

        @Min(value = 0, message = "debe ser mayor o igual a cero")
        Integer azucarPorLitro
) {
}
