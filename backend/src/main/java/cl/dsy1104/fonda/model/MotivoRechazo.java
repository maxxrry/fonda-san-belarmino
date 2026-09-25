package cl.dsy1104.fonda.model;

/** Por que se rechazo una venta. Se usa tambien como codigo de error en la respuesta 409. */
public enum MotivoRechazo {
    VENTA_RESTRINGIDA,
    LIMITE_EXCEDIDO,
    STOCK_INSUFICIENTE
}
