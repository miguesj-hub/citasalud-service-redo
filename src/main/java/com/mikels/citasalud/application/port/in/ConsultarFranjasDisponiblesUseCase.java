package com.mikels.citasalud.application.port.in;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import com.mikels.citasalud.domain.model.FranjaHoraria;

public interface ConsultarFranjasDisponiblesUseCase {

    List<FranjaHoraria> consultar(UUID medicoId, LocalDate fecha);
}
