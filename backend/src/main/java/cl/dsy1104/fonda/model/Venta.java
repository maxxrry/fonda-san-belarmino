package cl.dsy1104.fonda.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalDateTime;

/**
 * Intento de venta de una bebida. Se guarda siempre, autorizado o rechazado,
 * para que el historial muestre ambos.
 */
@Entity
@Table(name = "venta")
public class Venta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Muchas ventas apuntan a una misma bebida. LAZY: la bebida se carga solo
    // cuando se usa, asi que hay que leerla dentro de una transaccion.
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "bebida_id", nullable = false)
    private Bebida bebida;

    @Column(nullable = false)
    private int unidades;

    // 0 cuando la venta se rechaza.
    @Column(nullable = false)
    private int total;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoVenta estado;

    // Solo tiene valor cuando estado es RECHAZADA.
    @Enumerated(EnumType.STRING)
    private MotivoRechazo motivo;

    @Column(nullable = false)
    private LocalDateTime fecha;

    public Venta() {
    }

    // Se ejecuta justo antes del INSERT: toda venta queda con su fecha.
    @PrePersist
    void asignarFecha() {
        if (fecha == null) {
            fecha = LocalDateTime.now();
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Bebida getBebida() {
        return bebida;
    }

    public void setBebida(Bebida bebida) {
        this.bebida = bebida;
    }

    public int getUnidades() {
        return unidades;
    }

    public void setUnidades(int unidades) {
        this.unidades = unidades;
    }

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public EstadoVenta getEstado() {
        return estado;
    }

    public void setEstado(EstadoVenta estado) {
        this.estado = estado;
    }

    public MotivoRechazo getMotivo() {
        return motivo;
    }

    public void setMotivo(MotivoRechazo motivo) {
        this.motivo = motivo;
    }

    public LocalDateTime getFecha() {
        return fecha;
    }

    public void setFecha(LocalDateTime fecha) {
        this.fecha = fecha;
    }
}
