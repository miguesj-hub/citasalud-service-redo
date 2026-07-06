package com.mikels.citasalud.unit.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mikels.citasalud.application.port.out.CitaRepositoryPort;
import com.mikels.citasalud.application.port.out.FranjaHorariaRepositoryPort;
import com.mikels.citasalud.application.port.out.MedicoRepositoryPort;
import com.mikels.citasalud.application.port.out.NotificationPort;
import com.mikels.citasalud.application.port.out.PacienteRepositoryPort;
import com.mikels.citasalud.application.service.ReservarCitaService;
import com.mikels.citasalud.domain.exception.FranjaNoDisponibleException;
import com.mikels.citasalud.domain.model.Cita;
import com.mikels.citasalud.domain.model.FranjaHoraria;
import com.mikels.citasalud.domain.model.Medico;
import com.mikels.citasalud.domain.model.Paciente;

@ExtendWith(MockitoExtension.class)
class ReservarCitaServiceConflictoTest {

    @Mock
    private PacienteRepositoryPort pacienteRepositoryPort;
    @Mock
    private MedicoRepositoryPort medicoRepositoryPort;
    @Mock
    private FranjaHorariaRepositoryPort franjaHorariaRepositoryPort;
    @Mock
    private CitaRepositoryPort citaRepositoryPort;
    @Mock
    private NotificationPort notificationPort;

    private ReservarCitaService reservarCitaService;

    private UUID pacienteId;
    private UUID medicoId;
    private UUID franjaId;

    @BeforeEach
    void setUp() {
        reservarCitaService = new ReservarCitaService(
                pacienteRepositoryPort, medicoRepositoryPort, franjaHorariaRepositoryPort,
                citaRepositoryPort, notificationPort);

        pacienteId = UUID.randomUUID();
        medicoId = UUID.randomUUID();
        franjaId = UUID.randomUUID();
    }

    @Test
    void siLaFranjaYaEstaOcupadaLanzaFranjaNoDisponibleException() {
        Paciente paciente = Paciente.builder()
                .id(pacienteId).nombre("Juan Perez").numeroWhatsApp("+573001234567").build();
        Medico medico = Medico.builder()
                .id(medicoId).nombre("Dra. Ana Gomez").especialidad("Medicina General").build();
        FranjaHoraria franjaOcupada = FranjaHoraria.builder()
                .id(franjaId).medicoId(medicoId).fecha(LocalDate.of(2026, 7, 10))
                .horaInicio(LocalTime.of(8, 0)).horaFin(LocalTime.of(8, 30))
                .estado(FranjaHoraria.EstadoFranja.OCUPADA)
                .build();

        when(pacienteRepositoryPort.buscarPorId(pacienteId)).thenReturn(Optional.of(paciente));
        when(medicoRepositoryPort.buscarPorId(medicoId)).thenReturn(Optional.of(medico));
        when(franjaHorariaRepositoryPort.buscarPorId(franjaId)).thenReturn(Optional.of(franjaOcupada));

        assertThatThrownBy(() -> reservarCitaService.reservar(pacienteId, medicoId, franjaId))
                .isInstanceOf(FranjaNoDisponibleException.class);

        verify(citaRepositoryPort, never()).guardar(any(Cita.class));
    }
}
