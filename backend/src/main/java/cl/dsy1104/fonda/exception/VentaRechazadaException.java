package cl.dsy1104.fonda.exception;

import cl.dsy1104.fonda.model.MotivoRechazo;

/**
 * La venta no paso una de las verificaciones. Se lanza DESPUES de guardar la
 * venta rechazada. El manejador global la traduce a 409 con el motivo como codigo.
 */
public class VentaRechazadaException extends RuntimeException {

    private final MotivoRechazo motivo;

    public VentaRechazadaException(MotivoRechazo motivo, String mensaje) {
        super(mensaje);
        this.motivo = motivo;
    }

    public MotivoRechazo getMotivo() {
        return motivo;
    }
}
