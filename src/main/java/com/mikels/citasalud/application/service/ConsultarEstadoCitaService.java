package com.mikels.citasalud.application.service;

import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mikels.citasalud.application.port.in.ConsultarEstadoCitaUseCase;
import com.mikels.citasalud.application.port.out.CitaRepositoryPort;
import com.mikels.citasalud.domain.exception.RecursoNoEncontradoException;
import com.mikels.citasalud.domain.model.Cita;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultarEstadoCitaService implements ConsultarEstadoCitaUseCase {

    private final CitaRepositoryPort citaRepositoryPort;

    @Override
    public Cita consultar(UUID pacienteId, UUID franjaHorariaId) {
        return citaRepositoryPort.buscarPorPacienteYFranja(pacienteId, franjaHorariaId)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "No existe una cita confirmada para pacienteId=" + pacienteId
                                + " y franjaHorariaId=" + franjaHorariaId));
    }
}
