package com.mikels.citasalud.application.service;

import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.mikels.citasalud.application.port.in.ReservarCitaUseCase;
import com.mikels.citasalud.application.port.out.CitaRepositoryPort;
import com.mikels.citasalud.application.port.out.FranjaHorariaRepositoryPort;
import com.mikels.citasalud.application.port.out.MedicoRepositoryPort;
import com.mikels.citasalud.application.port.out.NotificationPort;
import com.mikels.citasalud.application.port.out.PacienteRepositoryPort;
import com.mikels.citasalud.domain.exception.FranjaNoDisponibleException;
import com.mikels.citasalud.domain.exception.RecursoNoEncontradoException;
import com.mikels.citasalud.domain.model.Cita;
import com.mikels.citasalud.domain.model.FranjaHoraria;
import com.mikels.citasalud.domain.model.Medico;
import com.mikels.citasalud.domain.model.Paciente;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReservarCitaService implements ReservarCitaUseCase {

    private static final String MENSAJE_FRANJA_NO_DISPONIBLE =
            "La franja horaria seleccionada ya no esta disponible. Por favor elige otra.";

    private final PacienteRepositoryPort pacienteRepositoryPort;
    private final MedicoRepositoryPort medicoRepositoryPort;
    private final FranjaHorariaRepositoryPort franjaHorariaRepositoryPort;
    private final CitaRepositoryPort citaRepositoryPort;
    private final NotificationPort notificationPort;

    @Override
    @Transactional
    public Cita reservar(UUID pacienteId, UUID medicoId, UUID franjaHorariaId) {
        Paciente paciente = pacienteRepositoryPort.buscarPorId(pacienteId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Paciente no encontrado: " + pacienteId));
        Medico medico = medicoRepositoryPort.buscarPorId(medicoId)
                .filter(Medico::isActivo)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medico no encontrado: " + medicoId));
        FranjaHoraria franja = franjaHorariaRepositoryPort.buscarPorId(franjaHorariaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "Franja horaria no encontrada: " + franjaHorariaId));

        if (franja.getEstado() != FranjaHoraria.EstadoFranja.DISPONIBLE
                || !franja.getMedicoId().equals(medico.getId())) {
            throw new FranjaNoDisponibleException(MENSAJE_FRANJA_NO_DISPONIBLE);
        }

        Cita citaGuardada;
        try {
            franjaHorariaRepositoryPort.marcarComoOcupada(franja);

            Cita nuevaCita = Cita.builder()
                    .id(UUID.randomUUID())
                    .pacienteId(paciente.getId())
                    .medicoId(medico.getId())
                    .franjaHorariaId(franja.getId())
                    .fecha(franja.getFecha())
                    .horaInicio(franja.getHoraInicio())
                    .estado(Cita.EstadoCita.CONFIRMADA)
                    .notificacionEnviada(false)
                    .creadoEn(Instant.now())
                    .build();

            citaGuardada = citaRepositoryPort.guardar(nuevaCita);
        } catch (DataIntegrityViolationException | ObjectOptimisticLockingFailureException ex) {
            // Otra solicitud confirmo la misma franja concurrentemente (FR-005/FR-009); la
            // restriccion unica de base de datos (o el bloqueo optimista) es la garantia final.
            throw new FranjaNoDisponibleException(MENSAJE_FRANJA_NO_DISPONIBLE);
        }

        NotificationPort.NotificationResult resultadoNotificacion =
                notificationPort.enviarConfirmacion(citaGuardada, paciente.getNumeroWhatsApp());

        return Cita.builder()
                .id(citaGuardada.getId())
                .pacienteId(citaGuardada.getPacienteId())
                .medicoId(citaGuardada.getMedicoId())
                .franjaHorariaId(citaGuardada.getFranjaHorariaId())
                .fecha(citaGuardada.getFecha())
                .horaInicio(citaGuardada.getHoraInicio())
                .estado(citaGuardada.getEstado())
                .notificacionEnviada(resultadoNotificacion.exitoso())
                .creadoEn(citaGuardada.getCreadoEn())
                .build();
    }
}
