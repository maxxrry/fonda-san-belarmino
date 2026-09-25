package cl.dsy1104.fonda.repository;

import cl.dsy1104.fonda.model.Bebida;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/** Acceso a la tabla bebida. Spring Data genera la implementacion al arrancar. */
public interface BebidaRepository extends JpaRepository<Bebida, Long> {

    // Spring Data arma la consulta a partir del nombre del metodo:
    // WHERE UPPER(nombre) LIKE UPPER('%' || ? || '%')
    List<Bebida> findByNombreContainingIgnoreCase(String nombre);
}
