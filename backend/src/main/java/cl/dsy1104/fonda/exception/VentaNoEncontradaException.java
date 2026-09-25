package cl.dsy1104.fonda.exception;

/** No existe una venta con el id pedido. El manejador global la traduce a 404. */
public class VentaNoEncontradaException extends RuntimeException {

    public VentaNoEncontradaException(Long id) {
        super("No existe la venta con id " + id + ".");
    }
}
