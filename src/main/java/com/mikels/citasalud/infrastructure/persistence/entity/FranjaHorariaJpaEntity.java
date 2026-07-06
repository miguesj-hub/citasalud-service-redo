package com.mikels.citasalud.infrastructure.persistence.entity;

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
import jakarta.persistence.Version;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "franja_horaria", uniqueConstraints = @UniqueConstraint(
        name = "uk_franja_medico_fecha_hora",
        columnNames = {"medico_id", "fecha", "hora_inicio"}))
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FranjaHorariaJpaEntity {

    @Id
    private UUID id;

    @Column(name = "medico_id", nullable = false)
    private UUID medicoId;

    @Column(nullable = false)
    private LocalDate fecha;

    @Column(name = "hora_inicio", nullable = false)
    private LocalTime horaInicio;

    @Column(name = "hora_fin", nullable = false)
    private LocalTime horaFin;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EstadoFranjaJpa estado;

    @Version
    private Long version;

    public enum EstadoFranjaJpa {
        DISPONIBLE,
        OCUPADA
    }
}
