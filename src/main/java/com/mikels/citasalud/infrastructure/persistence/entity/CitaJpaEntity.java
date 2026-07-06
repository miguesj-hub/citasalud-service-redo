package com.mikels.citasalud.infrastructure.persistence.entity;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cita", uniqueConstraints = @UniqueConstraint(
        name = "uk_cita_medico_fecha_hora",
        columnNames = {"medico_id", "fecha", "hora_inicio"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CitaJpaEntity {

    @Id
    private UUID id;

    @Column(name = "paciente_id", nullable = false)
    private UUID pacienteId;

    @Column(name = "medico_id", nullable = false)
    private UUID medicoId;

    @Column(name = "franja_horaria_id", nullable = false)
    private UUID franjaHorariaId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoCitaJpa estado;

    @Column(name = "notificacion_enviada", nullable = false)
    private boolean notificacionEnviada;

    @Column(name = "creado_en", nullable = false)
    private Instant creadoEn;

    public enum EstadoCitaJpa {
        CONFIRMADA
    }
}
