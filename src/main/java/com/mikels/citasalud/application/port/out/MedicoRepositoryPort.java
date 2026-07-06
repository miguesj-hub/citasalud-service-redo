package com.mikels.citasalud.application.port.out;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mikels.citasalud.domain.model.Medico;

public interface MedicoRepositoryPort {

    Optional<Medico> buscarPorId(UUID medicoId);

    List<Medico> listarTodos();
}
