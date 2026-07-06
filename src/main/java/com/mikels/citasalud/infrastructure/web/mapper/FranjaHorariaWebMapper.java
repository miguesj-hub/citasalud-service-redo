package com.mikels.citasalud.infrastructure.web.mapper;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.mikels.citasalud.domain.model.FranjaHoraria;
import com.mikels.citasalud.infrastructure.web.generated.model.FranjaHoraria.EstadoEnum;

@Component
public class FranjaHorariaWebMapper {

    private static final DateTimeFormatter HORA_FORMATO = DateTimeFormatter.ofPattern("HH:mm:ss");

    public com.mikels.citasalud.infrastructure.web.generated.model.FranjaHoraria toDto(FranjaHoraria franja) {
        com.mikels.citasalud.infrastructure.web.generated.model.FranjaHoraria dto =
                new com.mikels.citasalud.infrastructure.web.generated.model.FranjaHoraria();
        dto.setId(franja.getId());
        dto.setMedicoId(franja.getMedicoId());
        dto.setFecha(franja.getFecha());
        dto.setHoraInicio(franja.getHoraInicio().format(HORA_FORMATO));
        dto.setHoraFin(franja.getHoraFin().format(HORA_FORMATO));
        dto.setEstado(EstadoEnum.valueOf(franja.getEstado().name()));
        return dto;
    }
}
