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
 * Entidad de solo lectura: la creación/gestión de pacientes pertenece a una feature futura
 * (ver research.md §6). Esta feature únicamente lee esta tabla.
 */
@Entity
@Table(name = "paciente")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PacienteJpaEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String nombre;

    @Column(name = "numero_whatsapp", nullable = false)
    private String numeroWhatsApp;
}
