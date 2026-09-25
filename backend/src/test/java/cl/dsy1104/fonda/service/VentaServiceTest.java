package cl.dsy1104.fonda.service;

import cl.dsy1104.fonda.dto.VentaRequest;
import cl.dsy1104.fonda.dto.VentaResponse;
import cl.dsy1104.fonda.exception.BebidaNoEncontradaException;
import cl.dsy1104.fonda.exception.VentaRechazadaException;
import cl.dsy1104.fonda.model.Bebida;
import cl.dsy1104.fonda.model.EstadoVenta;
import cl.dsy1104.fonda.model.MotivoRechazo;
import cl.dsy1104.fonda.model.TipoBebida;
import cl.dsy1104.fonda.repository.BebidaRepository;
import cl.dsy1104.fonda.repository.VentaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Prueba VentaService contra H2 con los datos de data.sql. Sin @Transactional
 * en el test: se quiere ver lo que de verdad queda confirmado en la base.
 * Como los tests comparten la base, se comparan diferencias (antes/despues).
 */
@SpringBootTest
class VentaServiceTest {

    @Autowired
    private VentaService ventaService;

    @Autowired
    private BebidaRepository bebidaRepository;

    @Autowired
    private VentaRepository ventaRepository;

    @Value("${fonda.limite-unidades-por-cliente}")
    private int limite;

    @Test
    void ventaAutorizadaDescuentaStockYCalculaTotal() {
        Bebida pisco = buscar("Pisco Sour", TipoBebida.ALCOHOLICA);

        VentaResponse venta = ventaService.registrar(new VentaRequest(pisco.getId(), 2));

        assertEquals(EstadoVenta.AUTORIZADA, venta.estado());
        assertEquals(7000, venta.total());
        assertEquals(pisco.getStock() - 2, stockDe(pisco));
    }

    @Test
    void ventaRechazadaTambienQuedaGuardada() {
        Bebida pisco = buscar("Pisco Sour", TipoBebida.ALCOHOLICA);
        long ventasAntes = ventaRepository.count();

        VentaRechazadaException error = assertThrows(VentaRechazadaException.class,
                () -> ventaService.registrar(new VentaRequest(pisco.getId(), limite + 1)));

        assertEquals(MotivoRechazo.LIMITE_EXCEDIDO, error.getMotivo());
        assertEquals(ventasAntes + 1, ventaRepository.count());
        assertEquals(pisco.getStock(), stockDe(pisco));
    }

    @Test
    void restriccionSeVerificaAntesQueElLimite() {
        Bebida chicha = buscar("Chicha", TipoBebida.ALCOHOLICA);

        VentaRechazadaException error = assertThrows(VentaRechazadaException.class,
                () -> ventaService.registrar(new VentaRequest(chicha.getId(), limite + 1)));

        assertEquals(MotivoRechazo.VENTA_RESTRINGIDA, error.getMotivo());
    }

    @Test
    void sinAlcoholNoTieneLimitePeroSiStock() {
        Bebida mote = buscar("Mote con Huesillo", TipoBebida.SIN_ALCOHOL);

        VentaRechazadaException error = assertThrows(VentaRechazadaException.class,
                () -> ventaService.registrar(new VentaRequest(mote.getId(), mote.getStock() + 1)));

        assertEquals(MotivoRechazo.STOCK_INSUFICIENTE, error.getMotivo());
    }

    @Test
    void bebidaInexistenteNoGuardaVenta() {
        long ventasAntes = ventaRepository.count();

        assertThrows(BebidaNoEncontradaException.class,
                () -> ventaService.registrar(new VentaRequest(9999L, 1)));

        assertEquals(ventasAntes, ventaRepository.count());
    }

    private Bebida buscar(String nombre, TipoBebida tipo) {
        return bebidaRepository.findByNombreContainingIgnoreCase(nombre).stream()
                .filter(b -> b.getTipo() == tipo)
                .findFirst()
                .orElseThrow();
    }

    private int stockDe(Bebida bebida) {
        return bebidaRepository.findById(bebida.getId()).orElseThrow().getStock();
    }
}
