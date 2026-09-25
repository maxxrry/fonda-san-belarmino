package cl.dsy1104.fonda.dto;

import cl.dsy1104.fonda.model.Bebida;
import cl.dsy1104.fonda.model.TipoBebida;

/** Bebida tal como la entrega la API, con el precio ya calculado por el service. */
public record BebidaResponse(
        Long id,
        String nombre,
        TipoBebida tipo,
        int volumenML,
        int stock,
        Double gradosAlcohol,
        Boolean certificada,
        Integer azucarPorLitro,
        boolean ventaRestringida,
        int precio
) {

    /** Copia los datos de la entidad. El precio lo recibe: aqui no se calcula. */
    public static BebidaResponse desde(Bebida bebida, int precio) {
        return new BebidaResponse(
                bebida.getId(),
                bebida.getNombre(),
                bebida.getTipo(),
                bebida.getVolumenML(),
                bebida.getStock(),
                bebida.getGradosAlcohol(),
                bebida.getCertificada(),
                bebida.getAzucarPorLitro(),
                bebida.isVentaRestringida(),
                precio
        );
    }
}
