package com.mikels.citasalud.infrastructure.persistence.entity;

import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * Entidad de solo lectura: la gestión de médicos/agendas pertenece a una feature futura
 * (ver research.md §6). Esta feature únicamente lee esta tabla.
 */
@Entity
@Table(name = "medico")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicoJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(nullable = false)
    private String especialidad;

    @Column(nullable = false)
    private boolean activo;
}
