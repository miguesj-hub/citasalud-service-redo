package com.mikels.citasalud.infrastructure.persistence.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.mikels.citasalud.infrastructure.persistence.entity.FranjaHorariaJpaEntity;

public interface FranjaHorariaJpaRepository extends JpaRepository<FranjaHorariaJpaEntity, UUID> {

    List<FranjaHorariaJpaEntity> findByMedicoIdAndFechaAndEstado(
            UUID medicoId, LocalDate fecha, FranjaHorariaJpaEntity.EstadoFranjaJpa estado);
}
