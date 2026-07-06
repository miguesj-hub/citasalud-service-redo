package com.mikels.citasalud.integration.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

import com.mikels.citasalud.infrastructure.persistence.entity.CitaJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.entity.FranjaHorariaJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.repository.CitaJpaRepository;
import com.mikels.citasalud.infrastructure.persistence.repository.FranjaHorariaJpaRepository;

@DataJpaTest
class CitaPersistenceIT {

    @Autowired
    private FranjaHorariaJpaRepository franjaHorariaJpaRepository;

    @Autowired
    private CitaJpaRepository citaJpaRepository;

    @Test
    void alConfirmarCitaLaFranjaQuedaOcupada() {
        UUID medicoId = UUID.randomUUID();
        LocalDate fecha = LocalDate.of(2026, 8, 1);
        LocalTime horaInicio = LocalTime.of(10, 0);

        FranjaHorariaJpaEntity franja = franjaHorariaJpaRepository.save(FranjaHorariaJpaEntity.builder()
                .id(UUID.randomUUID())
                .medicoId(medicoId)
                .fecha(fecha)
                .horaInicio(horaInicio)
                .horaFin(LocalTime.of(10, 30))
                .estado(FranjaHorariaJpaEntity.EstadoFranjaJpa.DISPONIBLE)
                .build());

        citaJpaRepository.save(CitaJpaEntity.builder()
                .id(UUID.randomUUID())
                .pacienteId(UUID.randomUUID())
                .medicoId(medicoId)
                .franjaHorariaId(franja.getId())
                .fecha(fecha)
                .horaInicio(horaInicio)
                .estado(CitaJpaEntity.EstadoCitaJpa.CONFIRMADA)
                .notificacionEnviada(false)
                .creadoEn(Instant.now())
                .build());

        franja.setEstado(FranjaHorariaJpaEntity.EstadoFranjaJpa.OCUPADA);
        franjaHorariaJpaRepository.save(franja);

        FranjaHorariaJpaEntity actualizada = franjaHorariaJpaRepository.findById(franja.getId()).orElseThrow();
        assertThat(actualizada.getEstado()).isEqualTo(FranjaHorariaJpaEntity.EstadoFranjaJpa.OCUPADA);
    }
}
