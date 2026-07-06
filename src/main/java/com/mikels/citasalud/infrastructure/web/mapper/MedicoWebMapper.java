package com.mikels.citasalud.infrastructure.web.mapper;

import org.springframework.stereotype.Component;

import com.mikels.citasalud.domain.model.Medico;

@Component
public class MedicoWebMapper {

    public com.mikels.citasalud.infrastructure.web.generated.model.Medico toDto(Medico medico) {
        com.mikels.citasalud.infrastructure.web.generated.model.Medico dto =
                new com.mikels.citasalud.infrastructure.web.generated.model.Medico();
        dto.setId(medico.getId());
        dto.setNombre(medico.getNombre());
        dto.setEspecialidad(medico.getEspecialidad());
        return dto;
    }
}
