package cl.dsy1104.fonda.service;

import cl.dsy1104.fonda.model.Bebida;
import cl.dsy1104.fonda.model.TipoBebida;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Precio calculado con los mismos datos que carga data.sql. */
class BebidaServiceTest {

    // calcularPrecio no usa los repositorios, asi que no hace falta Spring.
    private final BebidaService service = new BebidaService(null, null);

    @Test
    void alcoholicaSinCertificarLlevaRecargoDe20() {
        assertEquals(4200, service.calcularPrecio(alcoholica(false)));
    }

    @Test
    void alcoholicaCertificadaPagaPrecioBase() {
        assertEquals(3500, service.calcularPrecio(alcoholica(true)));
    }

    @Test
    void sinAlcoholConMasDe80DeAzucarLlevaRecargoDe10() {
        assertEquals(2200, service.calcularPrecio(sinAlcohol(95)));
    }

    @Test
    void sinAlcoholCon80OMenosDeAzucarPagaPrecioBase() {
        assertEquals(2000, service.calcularPrecio(sinAlcohol(70)));
        assertEquals(2000, service.calcularPrecio(sinAlcohol(80)));
    }

    private Bebida alcoholica(boolean certificada) {
        Bebida bebida = new Bebida();
        bebida.setTipo(TipoBebida.ALCOHOLICA);
        bebida.setCertificada(certificada);
        return bebida;
    }

    private Bebida sinAlcohol(int azucar) {
        Bebida bebida = new Bebida();
        bebida.setTipo(TipoBebida.SIN_ALCOHOL);
        bebida.setAzucarPorLitro(azucar);
        return bebida;
    }
}
