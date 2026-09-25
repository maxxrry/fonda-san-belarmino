package cl.dsy1104.fonda.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * Bebida del catalogo. El precio NO se guarda: lo calcula el service a partir
 * del tipo y de los atributos propios de cada tipo.
 */
@Entity
@Table(name = "bebida")
public class Bebida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String nombre;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TipoBebida tipo;

    // Nombre explicito: la estrategia por defecto convertiria "volumenML" en
    // "volumenml", y data.sql usa "volumen_ml".
    @Column(name = "volumen_ml", nullable = false)
    private int volumenML;

    @Column(nullable = false)
    private int stock;

    // Solo para ALCOHOLICA; null en SIN_ALCOHOL.
    private Double gradosAlcohol;

    // Solo para ALCOHOLICA; null en SIN_ALCOHOL.
    private Boolean certificada;

    // Solo para SIN_ALCOHOL; null en ALCOHOLICA.
    private Integer azucarPorLitro;

    @Column(nullable = false)
    private boolean ventaRestringida;

    public Bebida() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public TipoBebida getTipo() {
        return tipo;
    }

    public void setTipo(TipoBebida tipo) {
        this.tipo = tipo;
    }

    public int getVolumenML() {
        return volumenML;
    }

    public void setVolumenML(int volumenML) {
        this.volumenML = volumenML;
    }

    public int getStock() {
        return stock;
    }

    public void setStock(int stock) {
        this.stock = stock;
    }

    public Double getGradosAlcohol() {
        return gradosAlcohol;
    }

    public void setGradosAlcohol(Double gradosAlcohol) {
        this.gradosAlcohol = gradosAlcohol;
    }

    public Boolean getCertificada() {
        return certificada;
    }

    public void setCertificada(Boolean certificada) {
        this.certificada = certificada;
    }

    public Integer getAzucarPorLitro() {
        return azucarPorLitro;
    }

    public void setAzucarPorLitro(Integer azucarPorLitro) {
        this.azucarPorLitro = azucarPorLitro;
    }

    public boolean isVentaRestringida() {
        return ventaRestringida;
    }

    public void setVentaRestringida(boolean ventaRestringida) {
        this.ventaRestringida = ventaRestringida;
    }
}
