package com.mikels.citasalud.infrastructure.persistence.adapter;

import org.springframework.stereotype.Component;

import com.mikels.citasalud.application.port.out.CitaRepositoryPort;
import com.mikels.citasalud.domain.model.Cita;
import com.mikels.citasalud.infrastructure.persistence.entity.CitaJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.repository.CitaJpaRepository;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class CitaRepositoryAdapter implements CitaRepositoryPort {

    private final CitaJpaRepository citaJpaRepository;

    @Override
    public Cita guardar(Cita cita) {
        CitaJpaEntity guardada = citaJpaRepository.save(toEntity(cita));
        return toDomain(guardada);
    }

    private CitaJpaEntity toEntity(Cita cita) {
        return CitaJpaEntity.builder()
                .id(cita.getId())
                .pacienteId(cita.getPacienteId())
                .medicoId(cita.getMedicoId())
                .franjaHorariaId(cita.getFranjaHorariaId())
                .fecha(cita.getFecha())
                .horaInicio(cita.getHoraInicio())
                .estado(CitaJpaEntity.EstadoCitaJpa.valueOf(cita.getEstado().name()))
                .notificacionEnviada(cita.isNotificacionEnviada())
                .creadoEn(cita.getCreadoEn())
                .build();
    }

    private Cita toDomain(CitaJpaEntity entity) {
        return Cita.builder()
                .id(entity.getId())
                .pacienteId(entity.getPacienteId())
                .medicoId(entity.getMedicoId())
                .franjaHorariaId(entity.getFranjaHorariaId())
                .fecha(entity.getFecha())
                .horaInicio(entity.getHoraInicio())
                .estado(Cita.EstadoCita.valueOf(entity.getEstado().name()))
                .notificacionEnviada(entity.isNotificacionEnviada())
                .creadoEn(entity.getCreadoEn())
                .build();
    }
}
