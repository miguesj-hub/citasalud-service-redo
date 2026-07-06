package com.mikels.citasalud.infrastructure.web.mapper;

import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.mikels.citasalud.domain.model.Cita;
import com.mikels.citasalud.infrastructure.web.generated.model.Cita.EstadoEnum;

@Component
public class CitaWebMapper {

    private static final DateTimeFormatter HORA_FORMATO = DateTimeFormatter.ofPattern("HH:mm:ss");

    public com.mikels.citasalud.infrastructure.web.generated.model.Cita toDto(Cita cita) {
        com.mikels.citasalud.infrastructure.web.generated.model.Cita dto =
                new com.mikels.citasalud.infrastructure.web.generated.model.Cita();
        dto.setId(cita.getId());
        dto.setPacienteId(cita.getPacienteId());
        dto.setMedicoId(cita.getMedicoId());
        dto.setFecha(cita.getFecha());
        dto.setHoraInicio(cita.getHoraInicio().format(HORA_FORMATO));
        dto.setEstado(EstadoEnum.valueOf(cita.getEstado().name()));
        dto.setNotificacionEnviada(cita.isNotificacionEnviada());
        return dto;
    }
}
