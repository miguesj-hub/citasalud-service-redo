package com.mikels.citasalud.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikels.citasalud.infrastructure.persistence.entity.FranjaHorariaJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.entity.MedicoJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.entity.PacienteJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.repository.FranjaHorariaJpaRepository;
import com.mikels.citasalud.infrastructure.persistence.repository.MedicoJpaRepository;
import com.mikels.citasalud.infrastructure.persistence.repository.PacienteJpaRepository;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class ReservaCitaExitosaSteps {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MedicoJpaRepository medicoJpaRepository;
    @Autowired
    private PacienteJpaRepository pacienteJpaRepository;
    @Autowired
    private FranjaHorariaJpaRepository franjaHorariaJpaRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID pacienteId;
    private UUID medicoId;
    private UUID franjaId;
    private MvcResult resultado;

    @Before
    public void prepararDatosDePrueba() {
        medicoId = UUID.randomUUID();
        pacienteId = UUID.randomUUID();
        franjaId = UUID.randomUUID();

        medicoJpaRepository.save(MedicoJpaEntity.builder()
                .id(medicoId).nombre("Dr. Prueba BDD").especialidad("Medicina General").build());
        pacienteJpaRepository.save(PacienteJpaEntity.builder()
                .id(pacienteId).nombre("Paciente BDD").numeroWhatsApp("+573000000001").build());
        franjaHorariaJpaRepository.save(FranjaHorariaJpaEntity.builder()
                .id(franjaId).medicoId(medicoId)
                .fecha(LocalDate.of(2026, 9, 1))
                .horaInicio(LocalTime.of(9, 0)).horaFin(LocalTime.of(9, 30))
                .estado(FranjaHorariaJpaEntity.EstadoFranjaJpa.DISPONIBLE)
                .build());
    }

    @Given("que el paciente accede al sistema fuera del horario de atención telefónica")
    public void queElPacienteAccedeFueraDeHorario() {
        // El sistema no impone restriccion horaria (FR-001); este paso documenta el contexto de negocio.
    }

    @When("elige un médico, una fecha y una hora disponibles y confirma la reserva")
    public void eligeMedicoFechaYHoraDisponiblesYConfirma() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(new SolicitudReserva(pacienteId, medicoId, franjaId));
        resultado = mockMvc.perform(post("/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andReturn();
    }

    @Then("la cita queda registrada con estado {string}")
    public void laCitaQuedaRegistradaConEstado(String estadoEsperado) throws Exception {
        assertThat(resultado.getResponse().getStatus()).isEqualTo(201);
        JsonNode cuerpo = objectMapper.readTree(resultado.getResponse().getContentAsString());
        assertThat(cuerpo.get("estado").asText()).isEqualTo(estadoEsperado);
    }

    @Then("el paciente recibe un intento de confirmación por WhatsApp")
    public void elPacienteRecibeIntentoDeConfirmacion() throws Exception {
        JsonNode cuerpo = objectMapper.readTree(resultado.getResponse().getContentAsString());
        assertThat(cuerpo.has("notificacionEnviada")).isTrue();
    }

    private record SolicitudReserva(UUID pacienteId, UUID medicoId, UUID franjaHorariaId) {
    }
}
