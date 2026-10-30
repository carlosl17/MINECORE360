
package com.electrodata.minecore360.service;

import com.electrodata.minecore360.entity.Inspeccion;
import com.electrodata.minecore360.repository.InspeccionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
public class InspeccionService {

    private final InspeccionRepository inspeccionRepository;

    // ==========================================
    // CONSTRUCTOR
    // ==========================================
    public InspeccionService(
            InspeccionRepository inspeccionRepository) {

        this.inspeccionRepository = inspeccionRepository;
    }

    // ==========================================
    // 1. LISTAR TODAS LAS INSPECCIONES
    // ==========================================
    @Transactional(readOnly = true)
    public List<Inspeccion> listarTodas() {

        return inspeccionRepository.findAll();
    }

    // ==========================================
    // 2. BUSCAR INSPECCION POR ID
    // ==========================================
    @Transactional(readOnly = true)
    public Optional<Inspeccion> buscarPorId(Long id) {

        return inspeccionRepository.findById(id);
    }

    // ==========================================
    // 3. GUARDAR O ACTUALIZAR INSPECCION
    // ==========================================
    @Transactional
    public Inspeccion guardar(Inspeccion inspeccion) {

        return inspeccionRepository.save(inspeccion);
    }

    // ==========================================
    // 4. ELIMINAR INSPECCION
    // ==========================================
    @Transactional
    public void eliminar(Long id) {

        inspeccionRepository.deleteById(id);
    }

    // ==========================================
    // 5. BUSCAR INSPECCIONES CON FILTROS
    // ==========================================
    @Transactional(readOnly = true)
    public List<Inspeccion> buscarConFiltros(
            Long camionId,
            String tecnico,
            LocalDate fecha,
            String estado) {

        // Normalizar técnico
        if (tecnico != null) {
            tecnico = tecnico.trim();

            if (tecnico.isEmpty()) {
                tecnico = null;
            }
        }

        // Normalizar estado
        if (estado != null) {
            estado = estado.trim();

            if (estado.isEmpty()) {
                estado = null;
            }
        }

        // Convertir fecha a rango del día
        LocalDateTime fechaInicio = null;
        LocalDateTime fechaFin = null;

        if (fecha != null) {

            fechaInicio = fecha.atStartOfDay();

            fechaFin = fecha.plusDays(1).atStartOfDay();
        }

        // Consultar MySQL mediante JPQL
        return inspeccionRepository.buscarConFiltros(
                camionId,
                tecnico,
                estado,
                fechaInicio,
                fechaFin
        );
    }
}
