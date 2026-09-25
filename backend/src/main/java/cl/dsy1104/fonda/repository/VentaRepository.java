package cl.dsy1104.fonda.repository;

import cl.dsy1104.fonda.model.Venta;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a la tabla venta. Guarda autorizadas y rechazadas por igual. */
public interface VentaRepository extends JpaRepository<Venta, Long> {

    // Historial con las ventas mas recientes primero: ORDER BY fecha DESC
    List<Venta> findAllByOrderByFechaDesc();

    // Hay alguna venta de esta bebida? Navega venta.bebida.id:
    // SELECT ... WHERE bebida_id = ? LIMIT 1
    boolean existsByBebidaId(Long bebidaId);
}
