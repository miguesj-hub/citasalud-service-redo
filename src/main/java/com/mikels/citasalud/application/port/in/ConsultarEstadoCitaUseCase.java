package com.mikels.citasalud.application.port.in;

import java.util.UUID;

import com.mikels.citasalud.domain.model.Cita;

public interface ConsultarEstadoCitaUseCase {

    /** FR-011: lanza RecursoNoEncontradoException si no existe una cita confirmada para esa combinación. */
    Cita consultar(UUID pacienteId, UUID franjaHorariaId);
}
