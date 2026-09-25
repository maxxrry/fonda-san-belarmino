package cl.dsy1104.fonda.dto;

import cl.dsy1104.fonda.model.EstadoVenta;
import cl.dsy1104.fonda.model.MotivoRechazo;
import cl.dsy1104.fonda.model.Venta;

import java.time.LocalDateTime;

/** Venta tal como la entrega la API, con el nombre de la bebida para el historial. */
public record VentaResponse(
        Long id,
        Long bebidaId,
        String nombre,
        int unidades,
        int total,
        EstadoVenta estado,
        MotivoRechazo motivo,
        LocalDateTime fecha
) {

    // Lee venta.getBebida(), que es LAZY: hay que llamarlo dentro de una transaccion.
    public static VentaResponse desde(Venta venta) {
        return new VentaResponse(
                venta.getId(),
                venta.getBebida().getId(),
                venta.getBebida().getNombre(),
                venta.getUnidades(),
                venta.getTotal(),
                venta.getEstado(),
                venta.getMotivo(),
                venta.getFecha()
        );
    }
}
