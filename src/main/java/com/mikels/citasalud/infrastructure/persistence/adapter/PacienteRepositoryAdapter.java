package com.mikels.citasalud.infrastructure.persistence.adapter;

import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.mikels.citasalud.application.port.out.PacienteRepositoryPort;
import com.mikels.citasalud.domain.model.Paciente;
import com.mikels.citasalud.infrastructure.persistence.entity.PacienteJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.repository.PacienteJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class PacienteRepositoryAdapter implements PacienteRepositoryPort {

    private final PacienteJpaRepository pacienteJpaRepository;

    @Override
    public Optional<Paciente> buscarPorId(UUID pacienteId) {
        return pacienteJpaRepository.findById(pacienteId).map(this::toDomain);
    }

    private Paciente toDomain(PacienteJpaEntity entity) {
        return Paciente.builder()
                .id(entity.getId())
                .nombre(entity.getNombre())
                .numeroWhatsApp(entity.getNumeroWhatsApp())
                .build();
    }
}
