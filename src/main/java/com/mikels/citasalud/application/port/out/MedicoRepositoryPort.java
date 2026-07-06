package com.mikels.citasalud.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mikels.citasalud.domain.model.Medico;

public interface MedicoRepositoryPort {

    /** Busca por id sin filtrar por estado, para poder rechazar explícitamente un médico dado de baja (FR-012). */
    Optional<Medico> buscarPorId(UUID medicoId);

    /** Solo médicos con {@code activo = true} (FR-002, FR-012). */
    List<Medico> listarActivos();
}
