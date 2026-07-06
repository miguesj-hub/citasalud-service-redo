package com.mikels.citasalud.bdd;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mikels.citasalud.infrastructure.persistence.entity.CitaJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.entity.FranjaHorariaJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.entity.MedicoJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.entity.PacienteJpaEntity;
import com.mikels.citasalud.infrastructure.persistence.repository.CitaJpaRepository;
import com.mikels.citasalud.infrastructure.persistence.repository.FranjaHorariaJpaRepository;
import com.mikels.citasalud.infrastructure.persistence.repository.MedicoJpaRepository;
import com.mikels.citasalud.infrastructure.persistence.repository.PacienteJpaRepository;

import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

public class FranjaYaOcupadaSteps {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MedicoJpaRepository medicoJpaRepository;
    @Autowired
    private PacienteJpaRepository pacienteJpaRepository;
    @Autowired
    private FranjaHorariaJpaRepository franjaHorariaJpaRepository;
    @Autowired
    private CitaJpaRepository citaJpaRepository;

    private final ObjectMapper objectMapper = new ObjectMapper();

    private UUID medicoId;
    private UUID franjaId;
    private UUID otroPacienteId;
    private MvcResult resultado;

    @Before
    public void prepararFranjaYaReservada() {
        medicoId = UUID.randomUUID();
        UUID primerPacienteId = UUID.randomUUID();
        otroPacienteId = UUID.randomUUID();
        franjaId = UUID.randomUUID();
        LocalDate fecha = LocalDate.of(2026, 9, 15);
        LocalTime horaInicio = LocalTime.of(10, 0);

        medicoJpaRepository.save(MedicoJpaEntity.builder()
                .id(medicoId).nombre("Dr. Prueba BDD 2").especialidad("Medicina General").build());
        pacienteJpaRepository.save(PacienteJpaEntity.builder()
                .id(primerPacienteId).nombre("Primer Paciente").numeroWhatsApp("+573000000002").build());
        pacienteJpaRepository.save(PacienteJpaEntity.builder()
                .id(otroPacienteId).nombre("Otro Paciente").numeroWhatsApp("+573000000003").build());

        franjaHorariaJpaRepository.save(FranjaHorariaJpaEntity.builder()
                .id(franjaId).medicoId(medicoId).fecha(fecha)
                .horaInicio(horaInicio).horaFin(LocalTime.of(10, 30))
                .estado(FranjaHorariaJpaEntity.EstadoFranjaJpa.OCUPADA)
                .build());

        citaJpaRepository.save(CitaJpaEntity.builder()
                .id(UUID.randomUUID())
                .pacienteId(primerPacienteId)
                .medicoId(medicoId)
                .franjaHorariaId(franjaId)
                .fecha(fecha)
                .horaInicio(horaInicio)
                .estado(CitaJpaEntity.EstadoCitaJpa.CONFIRMADA)
                .notificacionEnviada(true)
                .creadoEn(Instant.now())
                .build());
    }

    @Given("que existe una franja horaria ya reservada por otro paciente")
    public void queExisteUnaFranjaYaReservada() {
        // La franja y la primera cita ya fueron creadas en el hook @Before.
    }

    @When("el paciente intenta confirmar esa misma franja")
    public void elPacienteIntentaConfirmarEsaMismaFranja() throws Exception {
        String cuerpo = objectMapper.writeValueAsString(new SolicitudReserva(otroPacienteId, medicoId, franjaId));
        resultado = mockMvc.perform(post("/citas")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(cuerpo))
                .andReturn();
    }

    @Then("el sistema responde que la franja no está disponible")
    public void elSistemaRespondeQueLaFranjaNoEstaDisponible() throws Exception {
        assertThat(resultado.getResponse().getStatus()).isEqualTo(409);
        JsonNode cuerpo = objectMapper.readTree(resultado.getResponse().getContentAsString());
        assertThat(cuerpo.get("codigo").asText()).isEqualTo("FRANJA_NO_DISPONIBLE");
    }

    @Then("no se registra una segunda cita para esa franja")
    public void noSeRegistraUnaSegundaCitaParaEsaFranja() {
        long citasParaLaFranja = citaJpaRepository.findAll().stream()
                .filter(cita -> cita.getFranjaHorariaId().equals(franjaId))
                .count();
        assertThat(citasParaLaFranja).isEqualTo(1);
    }

    private record SolicitudReserva(UUID pacienteId, UUID medicoId, UUID franjaHorariaId) {
    }
}
