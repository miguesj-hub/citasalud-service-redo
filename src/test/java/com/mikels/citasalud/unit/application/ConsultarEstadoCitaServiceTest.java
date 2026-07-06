package com.mikels.citasalud.unit.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.mikels.citasalud.application.port.out.CitaRepositoryPort;
import com.mikels.citasalud.application.service.ConsultarEstadoCitaService;
import com.mikels.citasalud.domain.exception.RecursoNoEncontradoException;
import com.mikels.citasalud.domain.model.Cita;

/**
 * FR-011: consulta idempotente de estado de una cita por pacienteId + franjaHorariaId,
 * pensada para el Edge Case de pérdida de conexión (el paciente nunca conoció el id de la cita).
 */
@ExtendWith(MockitoExtension.class)
class ConsultarEstadoCitaServiceTest {

    @Mock
    private CitaRepositoryPort citaRepositoryPort;

    private ConsultarEstadoCitaService consultarEstadoCitaService;

    @Test
    void siLaCitaExisteLaRetorna() {
        consultarEstadoCitaService = new ConsultarEstadoCitaService(citaRepositoryPort);

        UUID pacienteId = UUID.randomUUID();
        UUID franjaId = UUID.randomUUID();
        Cita cita = Cita.builder()
                .id(UUID.randomUUID()).pacienteId(pacienteId).medicoId(UUID.randomUUID())
                .franjaHorariaId(franjaId).fecha(LocalDate.of(2026, 7, 10))
                .horaInicio(LocalTime.of(8, 0)).estado(Cita.EstadoCita.CONFIRMADA)
                .notificacionEnviada(true).creadoEn(Instant.now())
                .build();

        when(citaRepositoryPort.buscarPorPacienteYFranja(pacienteId, franjaId)).thenReturn(Optional.of(cita));

        Cita resultado = consultarEstadoCitaService.consultar(pacienteId, franjaId);

        assertThat(resultado.getId()).isEqualTo(cita.getId());
        assertThat(resultado.getEstado()).isEqualTo(Cita.EstadoCita.CONFIRMADA);
    }

    @Test
    void siLaCitaNoExisteLanzaRecursoNoEncontrado() {
        consultarEstadoCitaService = new ConsultarEstadoCitaService(citaRepositoryPort);

        UUID pacienteId = UUID.randomUUID();
        UUID franjaId = UUID.randomUUID();
        when(citaRepositoryPort.buscarPorPacienteYFranja(pacienteId, franjaId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> consultarEstadoCitaService.consultar(pacienteId, franjaId))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}
