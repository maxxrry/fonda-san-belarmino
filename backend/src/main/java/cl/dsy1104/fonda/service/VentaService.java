package cl.dsy1104.fonda.service;

import cl.dsy1104.fonda.dto.VentaRequest;
import cl.dsy1104.fonda.dto.VentaResponse;
import cl.dsy1104.fonda.exception.VentaRechazadaException;
import cl.dsy1104.fonda.model.Bebida;
import cl.dsy1104.fonda.model.EstadoVenta;
import cl.dsy1104.fonda.model.MotivoRechazo;
import cl.dsy1104.fonda.model.TipoBebida;
import cl.dsy1104.fonda.model.Venta;
import cl.dsy1104.fonda.repository.VentaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Reglas de negocio de la venta: verificaciones, descuento de stock y total. */
@Service
public class VentaService {

    private final VentaRepository ventaRepository;
    private final BebidaService bebidaService;
    private final int limiteUnidadesPorCliente;

    public VentaService(VentaRepository ventaRepository,
                        BebidaService bebidaService,
                        @Value("${fonda.limite-unidades-por-cliente}") int limiteUnidadesPorCliente) {
        this.ventaRepository = ventaRepository;
        this.bebidaService = bebidaService;
        this.limiteUnidadesPorCliente = limiteUnidadesPorCliente;
    }

    /**
     * Registra una venta. Si una verificacion falla, guarda la venta como
     * RECHAZADA y lanza VentaRechazadaException.
     *
     * noRollbackFor: por defecto una RuntimeException deshace la transaccion,
     * y con ella el INSERT de la venta rechazada. Asi la transaccion se
     * confirma igual y la rechazada queda en el historial.
     */
    @Transactional(noRollbackFor = VentaRechazadaException.class)
    public VentaResponse registrar(VentaRequest request) {
        // 404 si la bebida no existe. Aqui no se guarda nada: sin bebida no hay FK.
        Bebida bebida = bebidaService.obtener(request.bebidaId());
        int unidades = request.unidades();

        Venta venta = new Venta();
        venta.setBebida(bebida);
        venta.setUnidades(unidades);

        MotivoRechazo motivo = verificar(bebida, unidades);
        if (motivo != null) {
            venta.setEstado(EstadoVenta.RECHAZADA);
            venta.setMotivo(motivo);
            venta.setTotal(0);
            ventaRepository.save(venta);
            throw new VentaRechazadaException(motivo, mensajeDe(motivo, bebida, unidades));
        }

        // Bebida esta gestionada por JPA: el nuevo stock se guarda al confirmar.
        bebida.setStock(bebida.getStock() - unidades);
        venta.setEstado(EstadoVenta.AUTORIZADA);
        venta.setTotal(bebidaService.calcularPrecio(bebida) * unidades);
        return VentaResponse.desde(ventaRepository.save(venta));
    }

    @Transactional(readOnly = true)
    public List<VentaResponse> listar() {
        return ventaRepository.findAllByOrderByFechaDesc().stream()
                .map(VentaResponse::desde)
                .toList();
    }

    // Las verificaciones en el orden del enunciado; gana la primera que falla.
    private MotivoRechazo verificar(Bebida bebida, int unidades) {
        if (bebida.isVentaRestringida()) {
            return MotivoRechazo.VENTA_RESTRINGIDA;
        }
        if (bebida.getTipo() == TipoBebida.ALCOHOLICA && unidades > limiteUnidadesPorCliente) {
            return MotivoRechazo.LIMITE_EXCEDIDO;
        }
        if (bebida.getStock() < unidades) {
            return MotivoRechazo.STOCK_INSUFICIENTE;
        }
        return null;
    }

    private String mensajeDe(MotivoRechazo motivo, Bebida bebida, int unidades) {
        return switch (motivo) {
            case VENTA_RESTRINGIDA -> "La venta de " + bebida.getNombre() + " esta restringida.";
            case LIMITE_EXCEDIDO -> unidades + " unidades superan el limite de "
                    + limiteUnidadesPorCliente + " por cliente.";
            case STOCK_INSUFICIENTE -> "Stock insuficiente: se piden " + unidades
                    + " unidades y quedan " + bebida.getStock() + ".";
        };
    }
}
