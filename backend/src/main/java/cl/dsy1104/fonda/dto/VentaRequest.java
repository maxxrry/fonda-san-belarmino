package cl.dsy1104.fonda.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/** Datos que llegan en POST /api/ventas. */
public record VentaRequest(

        @NotNull(message = "es obligatorio")
        Long bebidaId,

        @NotNull(message = "es obligatorio")
        @Min(value = 1, message = "debe ser al menos 1")
        Integer unidades
) {
}
