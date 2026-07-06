package com.mikels.citasalud.application.port.in;

import java.util.UUID;

import com.mikels.citasalud.domain.model.Cita;

public interface ReservarCitaUseCase {

    Cita reservar(UUID pacienteId, UUID medicoId, UUID franjaHorariaId);
}
