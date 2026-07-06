package com.mikels.citasalud.application.port.out;

import com.mikels.citasalud.domain.model.Cita;

public interface CitaRepositoryPort {

    Cita guardar(Cita cita);
}
