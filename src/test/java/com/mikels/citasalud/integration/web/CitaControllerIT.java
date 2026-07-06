package com.mikels.citasalud.integration.web;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.mikels.citasalud.infrastructure.persistence.entity.FranjaHorariaJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.entity.MedicoJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.entity.PacienteJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.repository.FranjaHorariaJpaRepository;
import com.mikels.citasalud.infrastructure.persistence.repository.MedicoJpaRepository;
import com.mikels.citasalud.infrastructure.persistence.repository.PacienteJpaRepository;

/**
 * Cubre los endpoints auxiliares (listado de medicos, franjas disponibles) y el camino de
 * "recurso no encontrado" (404), complementando los escenarios BDD de reserva/conflicto.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CitaControllerIT {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MedicoJpaRepository medicoJpaRepository;
    @Autowired
    private PacienteJpaRepository pacienteJpaRepository;
    @Autowired
    private FranjaHorariaJpaRepository franjaHorariaJpaRepository;

    private UUID medicoId;
    private UUID pacienteId;
    private UUID franjaId;
    private LocalDate fecha;

    @BeforeEach
    void prepararDatos() {
        medicoId = UUID.randomUUID();
        pacienteId = UUID.randomUUID();
        franjaId = UUID.randomUUID();
        fecha = LocalDate.of(2026, 11, 1);

        medicoJpaRepository.save(MedicoJpaEntity.builder()
                .id(medicoId).nombre("Dr. Coverage").especialidad("Medicina General").build());
        pacienteJpaRepository.save(PacienteJpaEntity.builder()
                .id(pacienteId).nombre("Paciente Coverage").numeroWhatsApp("+573000000009").build());
        franjaHorariaJpaRepository.save(FranjaHorariaJpaEntity.builder()
                .id(franjaId).medicoId(medicoId).fecha(fecha)
                .horaInicio(LocalTime.of(14, 0)).horaFin(LocalTime.of(14, 30))
                .estado(FranjaHorariaJpaEntity.EstadoFranjaJpa.DISPONIBLE)
                .build());
    }

    @Test
    void listarMedicosDevuelveElMedicoCreado() throws Exception {
        mockMvc.perform(get("/medicos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(greaterThanOrEqualTo(1)));
    }

    @Test
    void listarFranjasDisponiblesDevuelveLaFranjaCreada() throws Exception {
        mockMvc.perform(get("/medicos/{medicoId}/franjas-disponibles", medicoId)
                        .param("fecha", fecha.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].estado").value("DISPONIBLE"));
    }

    @Test
    void listarFranjasDeUnMedicoInexistenteRetorna404() throws Exception {
        mockMvc.perform(get("/medicos/{medicoId}/franjas-disponibles", UUID.randomUUID())
                        .param("fecha", fecha.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }

    @Test
    void reservarCitaConPacienteInexistenteRetorna404() throws Exception {
        String cuerpo = """
                {"pacienteId":"%s","medicoId":"%s","franjaHorariaId":"%s"}
                """.formatted(UUID.randomUUID(), medicoId, franjaId);

        mockMvc.perform(post("/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.codigo").value("RECURSO_NO_ENCONTRADO"));
    }
}
