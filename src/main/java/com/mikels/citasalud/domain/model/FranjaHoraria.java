package com.mikels.citasalud.domain.model;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FranjaHoraria {

    private final UUID id;
    private final UUID medicoId;
    private final LocalDate fecha;
    private final LocalTime horaInicio;
    private final LocalTime horaFin;
    private final EstadoFranja estado;

    public enum EstadoFranja {
        DISPONIBLE,
        OCUPADA
    }
}
