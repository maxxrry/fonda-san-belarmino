package cl.dsy1104.fonda.exception;

/** No existe una bebida con el id pedido. El manejador global la traduce a 404. */
public class BebidaNoEncontradaException extends RuntimeException {

    public BebidaNoEncontradaException(Long id) {
        super("No existe la bebida con id " + id + ".");
    }
}
