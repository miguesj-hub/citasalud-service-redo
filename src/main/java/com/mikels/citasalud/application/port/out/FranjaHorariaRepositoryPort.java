package com.mikels.citasalud.application.port.out;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mikels.citasalud.domain.model.FranjaHoraria;

public interface FranjaHorariaRepositoryPort {

    List<FranjaHoraria> buscarDisponiblesPorMedicoYFecha(UUID medicoId, LocalDate fecha);

    Optional<FranjaHoraria> buscarPorId(UUID franjaHorariaId);

    FranjaHoraria marcarComoOcupada(FranjaHoraria franjaHoraria);
}
