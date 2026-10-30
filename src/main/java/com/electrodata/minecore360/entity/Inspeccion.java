
package com.electrodata.minecore360.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "inspecciones")
public class Inspeccion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Debe seleccionar un camión")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "camion_id", nullable = false)
    private Camion camion;

    @NotNull(message = "La fecha es obligatoria")
    @Column(name = "fecha_inspeccion", nullable = false)
    private LocalDateTime fechaInspeccion;

    @NotNull(message = "El horómetro es obligatorio")
    @DecimalMin(value = "0.0")
    @Digits(integer = 10, fraction = 2)
    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal horometro;

    @NotBlank(message = "El técnico es obligatorio")
    @Size(max = 100)
    @Column(nullable = false, length = 100)
    private String tecnico;

    // Usuario que creó la inspección.
    // Se asigna exclusivamente desde el servidor.
    @Column(name = "usuario_registro", length = 100)
    private String usuarioRegistro;

    // Subsistemas tecnológicos
    @Column(length = 20)
    private String cas;

    @Column(length = 20)
    private String oas;

    @Column(length = 20)
    private String mems;

    @Column(length = 20)
    private String rajant;

    @Column(length = 20)
    private String torsa;

    @Column(length = 20)
    private String gabinete;

    @Column(columnDefinition = "TEXT")
    private String observaciones;

    @NotBlank(message = "El estado es obligatorio")
    @Pattern(
        regexp = "BORRADOR|PENDIENTE|OBSERVADA|APROBADA",
        message = "Estado general no válido"
    )
    @Column(nullable = false, length = 20)
    private String estado = "BORRADOR";

    public Inspeccion() {
        this.fechaInspeccion = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Camion getCamion() {
        return camion;
    }

    public void setCamion(Camion camion) {
        this.camion = camion;
    }

    public LocalDateTime getFechaInspeccion() {
        return fechaInspeccion;
    }

    public void setFechaInspeccion(
            LocalDateTime fechaInspeccion) {
        this.fechaInspeccion = fechaInspeccion;
    }

    public BigDecimal getHorometro() {
        return horometro;
    }

    public void setHorometro(BigDecimal horometro) {
        this.horometro = horometro;
    }

    public String getTecnico() {
        return tecnico;
    }

    public void setTecnico(String tecnico) {
        this.tecnico = tecnico;
    }

    public String getUsuarioRegistro() {
        return usuarioRegistro;
    }

    public void setUsuarioRegistro(String usuarioRegistro) {
        this.usuarioRegistro = usuarioRegistro;
    }

    public String getCas() {
        return cas;
    }

    public void setCas(String cas) {
        this.cas = cas;
    }

    public String getOas() {
        return oas;
    }

    public void setOas(String oas) {
        this.oas = oas;
    }

    public String getMems() {
        return mems;
    }

    public void setMems(String mems) {
        this.mems = mems;
    }

    public String getRajant() {
        return rajant;
    }

    public void setRajant(String rajant) {
        this.rajant = rajant;
    }

    public String getTorsa() {
        return torsa;
    }

    public void setTorsa(String torsa) {
        this.torsa = torsa;
    }

    public String getGabinete() {
        return gabinete;
    }

    public void setGabinete(String gabinete) {
        this.gabinete = gabinete;
    }

    public String getObservaciones() {
        return observaciones;
    }

    public void setObservaciones(String observaciones) {
        this.observaciones = observaciones;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
