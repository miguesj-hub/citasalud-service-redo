package com.mikels.citasalud.application.port.in;

import java.util.List;

import com.mikels.citasalud.domain.model.Medico;

public interface ListarMedicosUseCase {

    List<Medico> listar();
}
