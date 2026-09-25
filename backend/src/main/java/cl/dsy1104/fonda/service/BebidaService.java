package cl.dsy1104.fonda.service;

import cl.dsy1104.fonda.dto.BebidaRequest;
import cl.dsy1104.fonda.dto.BebidaResponse;
import cl.dsy1104.fonda.exception.BebidaConVentasException;
import cl.dsy1104.fonda.exception.BebidaNoEncontradaException;
import cl.dsy1104.fonda.model.Bebida;
import cl.dsy1104.fonda.model.TipoBebida;
import cl.dsy1104.fonda.repository.BebidaRepository;
import cl.dsy1104.fonda.repository.VentaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/** Reglas de negocio del catalogo: precio, alta, edicion, baja y restriccion. */
@Service
public class BebidaService {

    private static final int PRECIO_BASE_ALCOHOLICA = 3500;
    private static final int PRECIO_BASE_SIN_ALCOHOL = 2000;
    private static final int RECARGO_SIN_CERTIFICAR_PORCIENTO = 20;
    private static final int RECARGO_AZUCAR_PORCIENTO = 10;
    private static final int AZUCAR_MAXIMA_SIN_RECARGO = 80;

    private final BebidaRepository bebidaRepository;
    private final VentaRepository ventaRepository;

    public BebidaService(BebidaRepository bebidaRepository, VentaRepository ventaRepository) {
        this.bebidaRepository = bebidaRepository;
        this.ventaRepository = ventaRepository;
    }

    @Transactional(readOnly = true)
    public List<BebidaResponse> listar(String nombre) {
        List<Bebida> bebidas = (nombre == null || nombre.isBlank())
                ? bebidaRepository.findAll()
                : bebidaRepository.findByNombreContainingIgnoreCase(nombre.trim());
        return bebidas.stream().map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public BebidaResponse buscarPorId(Long id) {
        return aResponse(obtener(id));
    }

    @Transactional
    public BebidaResponse crear(BebidaRequest request) {
        Bebida bebida = new Bebida();
        copiarDatos(request, bebida);
        // Toda bebida nueva parte sin restriccion; se restringe solo con PATCH.
        bebida.setVentaRestringida(false);
        return aResponse(bebidaRepository.save(bebida));
    }

    @Transactional
    public BebidaResponse actualizar(Long id, BebidaRequest request) {
        Bebida bebida = obtener(id);
        copiarDatos(request, bebida);
        return aResponse(bebidaRepository.save(bebida));
    }

    @Transactional
    public void eliminar(Long id) {
        Bebida bebida = obtener(id);
        if (ventaRepository.existsByBebidaId(id)) {
            throw new BebidaConVentasException(id);
        }
        bebidaRepository.delete(bebida);
    }

    @Transactional
    public BebidaResponse restringir(Long id) {
        Bebida bebida = obtener(id);
        bebida.setVentaRestringida(true);
        return aResponse(bebidaRepository.save(bebida));
    }

    /**
     * Precio de venta segun el tipo. Publico porque VentaService lo usa para
     * calcular el total: la regla vive en un solo lugar.
     */
    public int calcularPrecio(Bebida bebida) {
        if (bebida.getTipo() == TipoBebida.ALCOHOLICA) {
            // null cuenta como "sin certificacion": solo true evita el recargo.
            boolean certificada = Boolean.TRUE.equals(bebida.getCertificada());
            return certificada
                    ? PRECIO_BASE_ALCOHOLICA
                    : conRecargo(PRECIO_BASE_ALCOHOLICA, RECARGO_SIN_CERTIFICAR_PORCIENTO);
        }
        Integer azucar = bebida.getAzucarPorLitro();
        boolean muchaAzucar = azucar != null && azucar > AZUCAR_MAXIMA_SIN_RECARGO;
        return muchaAzucar
                ? conRecargo(PRECIO_BASE_SIN_ALCOHOL, RECARGO_AZUCAR_PORCIENTO)
                : PRECIO_BASE_SIN_ALCOHOL;
    }

    /** Busca la entidad o lanza 404. VentaService tambien la usa. */
    @Transactional(readOnly = true)
    public Bebida obtener(Long id) {
        return bebidaRepository.findById(id)
                .orElseThrow(() -> new BebidaNoEncontradaException(id));
    }

    // Aritmetica entera: 3500 * 120 / 100 = 4200 exacto, sin errores de double.
    private int conRecargo(int base, int porciento) {
        return base * (100 + porciento) / 100;
    }

    // ventaRestringida no se copia: el PUT no puede quitar una restriccion.
    private void copiarDatos(BebidaRequest request, Bebida bebida) {
        bebida.setNombre(request.nombre().trim());
        bebida.setTipo(request.tipo());
        bebida.setVolumenML(request.volumenML());
        bebida.setStock(request.stock());
        bebida.setGradosAlcohol(request.gradosAlcohol());
        bebida.setCertificada(request.certificada());
        bebida.setAzucarPorLitro(request.azucarPorLitro());
    }

    private BebidaResponse aResponse(Bebida bebida) {
        return BebidaResponse.desde(bebida, calcularPrecio(bebida));
    }
}
