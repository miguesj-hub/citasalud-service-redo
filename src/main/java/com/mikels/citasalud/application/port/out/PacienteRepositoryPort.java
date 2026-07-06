package com.mikels.citasalud.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.mikels.citasalud.domain.model.Paciente;

public interface PacienteRepositoryPort {

    Optional<Paciente> buscarPorId(UUID pacienteId);
}
