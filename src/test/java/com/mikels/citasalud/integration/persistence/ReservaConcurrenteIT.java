package com.mikels.citasalud.integration.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;

import com.mikels.citasalud.infrastructure.persistence.entity.CitaJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.entity.FranjaHorariaJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.entity.MedicoJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.repository.CitaJpaRepository;
import com.mikels.citasalud.infrastructure.persistence.repository.FranjaHorariaJpaRepository;
import com.mikels.citasalud.infrastructure.persistence.repository.MedicoJpaRepository;

@SpringBootTest
class ReservaConcurrenteIT {

    @Autowired
    private MedicoJpaRepository medicoJpaRepository;
    @Autowired
    private FranjaHorariaJpaRepository franjaHorariaJpaRepository;
    @Autowired
    private CitaJpaRepository citaJpaRepository;

    @Test
    void soloUnaDeDosInsercionesConcurrentesTieneExito() throws InterruptedException {
        UUID medicoId = UUID.randomUUID();
        LocalDate fecha = LocalDate.of(2026, 10, 1);
        LocalTime horaInicio = LocalTime.of(11, 0);

        medicoJpaRepository.save(MedicoJpaEntity.builder()
                .id(medicoId).nombre("Dr. Concurrencia").especialidad("Medicina General").build());
        UUID franjaId = UUID.randomUUID();
        franjaHorariaJpaRepository.save(FranjaHorariaJpaEntity.builder()
                .id(franjaId).medicoId(medicoId).fecha(fecha)
                .horaInicio(horaInicio).horaFin(LocalTime.of(11, 30))
                .estado(FranjaHorariaJpaEntity.EstadoFranjaJpa.DISPONIBLE)
                .build());

        int hilos = 2;
        ExecutorService executor = Executors.newFixedThreadPool(hilos);
        CountDownLatch listos = new CountDownLatch(hilos);
        CountDownLatch salida = new CountDownLatch(1);
        AtomicInteger exitos = new AtomicInteger();
        AtomicInteger fallos = new AtomicInteger();

        for (int i = 0; i < hilos; i++) {
            executor.submit(() -> {
                try {
                    listos.countDown();
                    salida.await();
                    citaJpaRepository.save(CitaJpaEntity.builder()
                            .id(UUID.randomUUID())
                            .pacienteId(UUID.randomUUID())
                            .medicoId(medicoId)
                            .franjaHorariaId(franjaId)
                            .fecha(fecha)
                            .horaInicio(horaInicio)
                            .estado(CitaJpaEntity.EstadoCitaJpa.CONFIRMADA)
                            .notificacionEnviada(false)
                            .creadoEn(Instant.now())
                            .build());
                    exitos.incrementAndGet();
                } catch (DataIntegrityViolationException ex) {
                    fallos.incrementAndGet();
                } catch (InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        listos.await();
        salida.countDown();
        executor.shutdown();
        boolean terminado = executor.awaitTermination(10, TimeUnit.SECONDS);

        assertThat(terminado).isTrue();
        assertThat(exitos.get()).isEqualTo(1);
        assertThat(fallos.get()).isEqualTo(1);
    }
}
