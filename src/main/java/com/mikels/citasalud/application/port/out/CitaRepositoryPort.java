package com.mikels.citasalud.application.port.out;

import java.util.Optional;
import java.util.UUID;

import com.mikels.citasalud.domain.model.Cita;

public interface CitaRepositoryPort {

    Cita guardar(Cita cita);

    /** FR-011: consulta idempotente de estado, sin necesitar el id de cita generado por el servidor. */
    Optional<Cita> buscarPorPacienteYFranja(UUID pacienteId, UUID franjaHorariaId);
}
