package com.mikels.citasalud.unit.application;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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
import com.mikels.citasalud.domain.exception.RecursoNoEncontradoException;
import com.mikels.citasalud.domain.model.Cita;
import com.mikels.citasalud.domain.model.Medico;
import com.mikels.citasalud.domain.model.Paciente;

/**
 * FR-012: un medico dado de baja (activo=false) debe tratarse como si no existiera.
 */
@ExtendWith(MockitoExtension.class)
class ReservarCitaServiceMedicoInactivoTest {

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

    @BeforeEach
    void setUp() {
        reservarCitaService = new ReservarCitaService(
                pacienteRepositoryPort, medicoRepositoryPort, franjaHorariaRepositoryPort,
                citaRepositoryPort, notificationPort);
    }

    @Test
    void siElMedicoEstaDadoDeBajaLanzaRecursoNoEncontrado() {
        UUID pacienteId = UUID.randomUUID();
        UUID medicoId = UUID.randomUUID();
        UUID franjaId = UUID.randomUUID();

        Paciente paciente = Paciente.builder()
                .id(pacienteId).nombre("Juan Perez").numeroWhatsApp("+573001234567").build();
        Medico medicoInactivo = Medico.builder()
                .id(medicoId).nombre("Dr. Retirado").especialidad("Medicina General").activo(false).build();

        when(pacienteRepositoryPort.buscarPorId(pacienteId)).thenReturn(Optional.of(paciente));
        when(medicoRepositoryPort.buscarPorId(medicoId)).thenReturn(Optional.of(medicoInactivo));

        assertThatThrownBy(() -> reservarCitaService.reservar(pacienteId, medicoId, franjaId))
                .isInstanceOf(RecursoNoEncontradoException.class);

        verify(franjaHorariaRepositoryPort, never()).buscarPorId(any());
        verify(citaRepositoryPort, never()).guardar(any(Cita.class));
    }
}
