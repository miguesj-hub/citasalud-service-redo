package com.mikels.citasalud.application.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.mikels.citasalud.application.port.in.ListarMedicosUseCase;
import com.mikels.citasalud.application.port.out.MedicoRepositoryPort;
import com.mikels.citasalud.domain.model.Medico;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ListarMedicosService implements ListarMedicosUseCase {

    private final MedicoRepositoryPort medicoRepositoryPort;

    @Override
    public List<Medico> listar() {
        return medicoRepositoryPort.listarActivos();
    }
}
