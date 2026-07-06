package com.mikels.citasalud.domain.model;

import java.util.UUID;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class Paciente {

    private final UUID id;
    private final String nombre;
    private final String numeroWhatsApp;
}
