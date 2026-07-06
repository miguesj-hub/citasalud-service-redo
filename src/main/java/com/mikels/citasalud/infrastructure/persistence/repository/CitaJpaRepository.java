package com.mikels.citasalud.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mikels.citasalud.infrastructure.persistence.entity.CitaJpaEntity;

public interface CitaJpaRepository extends JpaRepository<CitaJpaEntity, UUID> {
}
