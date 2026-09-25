package cl.dsy1104.fonda.dto;

import cl.dsy1104.fonda.model.TipoBebida;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Datos que llegan en POST y PUT de /api/bebidas. El controller lo valida con
 * @Valid antes de pasarlo al service.
 *
 * Pendiente: las reglas que dependen del tipo (gradosAlcohol y azucarPorLitro
 * obligatorios o nulos segun el tipo) van en un validador propio.
 */
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

        Double gradosAlcohol,

        Boolean certificada,

        Integer azucarPorLitro
) {
}
