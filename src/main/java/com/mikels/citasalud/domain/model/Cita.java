package com.mikels.citasalud.domain.model;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Cita {

    private final UUID id;
    private final UUID pacienteId;
    private final UUID medicoId;
    private final UUID franjaHorariaId;
    private final LocalDate fecha;
    private final LocalTime horaInicio;
    private final EstadoCita estado;
    private final boolean notificacionEnviada;
    private final Instant creadoEn;

    public enum EstadoCita {
        CONFIRMADA
    }
}
