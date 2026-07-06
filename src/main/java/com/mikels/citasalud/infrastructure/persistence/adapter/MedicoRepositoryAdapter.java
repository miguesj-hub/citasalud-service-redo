package com.mikels.citasalud.infrastructure.persistence.adapter;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.mikels.citasalud.application.port.out.MedicoRepositoryPort;
import com.mikels.citasalud.domain.model.Medico;
import com.mikels.citasalud.infrastructure.persistence.entity.MedicoJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.repository.MedicoJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MedicoRepositoryAdapter implements MedicoRepositoryPort {

    private final MedicoJpaRepository medicoJpaRepository;

    @Override
    public Optional<Medico> buscarPorId(UUID medicoId) {
        return medicoJpaRepository.findById(medicoId).map(this::toDomain);
    }

    @Override
    public List<Medico> listarActivos() {
        return medicoJpaRepository.findByActivoTrue().stream().map(this::toDomain).toList();
    }

    private Medico toDomain(MedicoJpaEntity entity) {
        return Medico.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .especialidad(entity.getEspecialidad())
                .activo(entity.isActivo())
                .build();
    }
}
