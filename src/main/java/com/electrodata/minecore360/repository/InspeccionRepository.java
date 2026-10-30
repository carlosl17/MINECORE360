
package com.electrodata.minecore360.repository;

import com.electrodata.minecore360.entity.Inspeccion;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;

public interface InspeccionRepository
        extends JpaRepository<Inspeccion, Long> {

    // ==========================================
    // BUSCAR INSPECCIONES CON FILTROS OPCIONALES
    // ==========================================
    @Query("""
        SELECT i
        FROM Inspeccion i
        JOIN FETCH i.camion c
        WHERE (:camionId IS NULL OR c.id = :camionId)
          AND (
              :tecnico IS NULL
              OR LOWER(i.tecnico) LIKE LOWER(
                  CONCAT('%', :tecnico, '%')
              )
          )
          AND (:estado IS NULL OR i.estado = :estado)
          AND (
              :fechaInicio IS NULL
              OR i.fechaInspeccion >= :fechaInicio
          )
          AND (
              :fechaFin IS NULL
              OR i.fechaInspeccion < :fechaFin
          )
        ORDER BY i.fechaInspeccion DESC
        """)
    List<Inspeccion> buscarConFiltros(
            @Param("camionId") Long camionId,
            @Param("tecnico") String tecnico,
            @Param("estado") String estado,
            @Param("fechaInicio") LocalDateTime fechaInicio,
            @Param("fechaFin") LocalDateTime fechaFin
    );
}
