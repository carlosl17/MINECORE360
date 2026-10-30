
package com.electrodata.minecore360.repository;

import com.electrodata.minecore360.entity.Camion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CamionRepository extends JpaRepository<Camion, Long> {

    boolean existsByCodigo(String codigo);

    Optional<Camion> findByCodigo(String codigo);
}

