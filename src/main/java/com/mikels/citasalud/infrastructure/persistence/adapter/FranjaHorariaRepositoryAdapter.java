package com.mikels.citasalud.infrastructure.persistence.adapter;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.stereotype.Component;

import com.mikels.citasalud.application.port.out.FranjaHorariaRepositoryPort;
import com.mikels.citasalud.domain.model.FranjaHoraria;
import com.mikels.citasalud.infrastructure.persistence.entity.FranjaHorariaJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.repository.FranjaHorariaJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FranjaHorariaRepositoryAdapter implements FranjaHorariaRepositoryPort {

    private final FranjaHorariaJpaRepository franjaHorariaJpaRepository;

    @Override
    public List<FranjaHoraria> buscarDisponiblesPorMedicoYFecha(UUID medicoId, LocalDate fecha) {
        return franjaHorariaJpaRepository
                .findByMedicoIdAndFechaAndEstado(medicoId, fecha, FranjaHorariaJpaEntity.EstadoFranjaJpa.DISPONIBLE)
                .stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<FranjaHoraria> buscarPorId(UUID franjaHorariaId) {
        return franjaHorariaJpaRepository.findById(franjaHorariaId).map(this::toDomain);
    }

    @Override
    public FranjaHoraria marcarComoOcupada(FranjaHoraria franjaHoraria) {
        FranjaHorariaJpaEntity entity = franjaHorariaJpaRepository.findById(franjaHoraria.getId())
                .orElseThrow(() -> new IllegalStateException(
                        "FranjaHoraria no encontrada: " + franjaHoraria.getId()));
        entity.setEstado(FranjaHorariaJpaEntity.EstadoFranjaJpa.OCUPADA);
        FranjaHorariaJpaEntity actualizada = franjaHorariaJpaRepository.save(entity);
        return toDomain(actualizada);
    }

    private FranjaHoraria toDomain(FranjaHorariaJpaEntity entity) {
        return FranjaHoraria.builder()
                .id(entity.getId())
                .medicoId(entity.getMedicoId())
                .fecha(entity.getFecha())
                .horaInicio(entity.getHoraInicio())
                .horaFin(entity.getHoraFin())
                .estado(FranjaHoraria.EstadoFranja.valueOf(entity.getEstado().name()))
                .build();
    }
}
