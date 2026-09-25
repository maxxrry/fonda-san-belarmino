package cl.dsy1104.fonda.exception;

/**
 * Se intento eliminar una bebida que ya tiene ventas registradas. Borrarla
 * romperia el historial. El manejador global la traduce a 409.
 */
public class BebidaConVentasException extends RuntimeException {

    public BebidaConVentasException(Long id) {
        super("La bebida con id " + id + " tiene ventas registradas y no se puede eliminar.");
    }
}
