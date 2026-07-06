package com.mikels.citasalud.application.service;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.stereotype.Service;

import com.mikels.citasalud.application.port.in.ConsultarFranjasDisponiblesUseCase;
import com.mikels.citasalud.application.port.out.FranjaHorariaRepositoryPort;
import com.mikels.citasalud.application.port.out.MedicoRepositoryPort;
import com.mikels.citasalud.domain.exception.RecursoNoEncontradoException;
import com.mikels.citasalud.domain.model.FranjaHoraria;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ConsultarFranjasDisponiblesService implements ConsultarFranjasDisponiblesUseCase {

    private final MedicoRepositoryPort medicoRepositoryPort;
    private final FranjaHorariaRepositoryPort franjaHorariaRepositoryPort;

    @Override
    public List<FranjaHoraria> consultar(UUID medicoId, LocalDate fecha) {
        medicoRepositoryPort.buscarPorId(medicoId)
                .orElseThrow(() -> new RecursoNoEncontradoException("Medico no encontrado: " + medicoId));
        return franjaHorariaRepositoryPort.buscarDisponiblesPorMedicoYFecha(medicoId, fecha);
    }
}
