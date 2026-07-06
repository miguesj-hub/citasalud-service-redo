package com.mikels.citasalud.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mikels.citasalud.infrastructure.persistence.entity.PacienteJpaEntity;

public interface PacienteJpaRepository extends JpaRepository<PacienteJpaEntity, UUID> {
}
