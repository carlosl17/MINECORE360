
package com.electrodata.minecore360.service;

import com.electrodata.minecore360.entity.Camion;
import com.electrodata.minecore360.repository.CamionRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CamionService {

    private final CamionRepository camionRepository;

    public CamionService(CamionRepository camionRepository) {
        this.camionRepository = camionRepository;
    }

    @Transactional(readOnly = true)
    public List<Camion> listarTodos() {
        return camionRepository.findAll();
    }

    @Transactional(readOnly = true)
    public Optional<Camion> buscarPorId(Long id) {
        return camionRepository.findById(id);
    }

    @Transactional(readOnly = true)
    public boolean existeCodigo(String codigo) {
        return camionRepository.existsByCodigo(codigo);
    }

    @Transactional
    public Camion guardar(Camion camion) {

        String codigo = camion.getCodigo().trim();
        camion.setCodigo(codigo);

        Optional<Camion> existente =
                camionRepository.findByCodigo(codigo);

        if (existente.isPresent() &&
            !existente.get().getId().equals(camion.getId())) {

            throw new IllegalArgumentException(
                "Ya existe un camión con ese código"
            );
        }

        return camionRepository.save(camion);
    }
}
