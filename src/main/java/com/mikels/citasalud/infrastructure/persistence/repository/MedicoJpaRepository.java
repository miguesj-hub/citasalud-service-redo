package com.mikels.citasalud.infrastructure.persistence.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mikels.citasalud.infrastructure.persistence.entity.MedicoJpaEntity;

public interface MedicoJpaRepository extends JpaRepository<MedicoJpaEntity, UUID> {
}
