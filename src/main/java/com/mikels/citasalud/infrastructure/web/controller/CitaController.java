package com.mikels.citasalud.infrastructure.web.controller;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.mikels.citasalud.application.port.in.ConsultarFranjasDisponiblesUseCase;
import com.mikels.citasalud.application.port.in.ListarMedicosUseCase;
import com.mikels.citasalud.application.port.in.ReservarCitaUseCase;
import com.mikels.citasalud.domain.model.Cita;
import com.mikels.citasalud.domain.model.FranjaHoraria;
import com.mikels.citasalud.domain.model.Medico;
import com.mikels.citasalud.infrastructure.web.generated.api.CitasApi;
import com.mikels.citasalud.infrastructure.web.generated.api.FranjasApi;
import com.mikels.citasalud.infrastructure.web.generated.api.MedicosApi;
import com.mikels.citasalud.infrastructure.web.generated.model.ReservaCitaRequest;
import com.mikels.citasalud.infrastructure.web.mapper.CitaWebMapper;
import com.mikels.citasalud.infrastructure.web.mapper.FranjaHorariaWebMapper;
import com.mikels.citasalud.infrastructure.web.mapper.MedicoWebMapper;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CitaController implements MedicosApi, FranjasApi, CitasApi {

    private final ListarMedicosUseCase listarMedicosUseCase;
    private final ConsultarFranjasDisponiblesUseCase consultarFranjasDisponiblesUseCase;
    private final ReservarCitaUseCase reservarCitaUseCase;
    private final MedicoWebMapper medicoWebMapper;
    private final FranjaHorariaWebMapper franjaHorariaWebMapper;
    private final CitaWebMapper citaWebMapper;

    @Override
    public ResponseEntity<List<com.mikels.citasalud.infrastructure.web.generated.model.Medico>> listarMedicos() {
        List<Medico> medicos = listarMedicosUseCase.listar();
        return ResponseEntity.ok(medicos.stream().map(medicoWebMapper::toDto).toList());
    }

    @Override
    public ResponseEntity<List<com.mikels.citasalud.infrastructure.web.generated.model.FranjaHoraria>> listarFranjasDisponibles(
            UUID medicoId, LocalDate fecha) {
        List<FranjaHoraria> franjas = consultarFranjasDisponiblesUseCase.consultar(medicoId, fecha);
        return ResponseEntity.ok(franjas.stream().map(franjaHorariaWebMapper::toDto).toList());
    }

    @Override
    public ResponseEntity<com.mikels.citasalud.infrastructure.web.generated.model.Cita> reservarCita(
            ReservaCitaRequest reservaCitaRequest) {
        Cita cita = reservarCitaUseCase.reservar(
                reservaCitaRequest.getPacienteId(),
                reservaCitaRequest.getMedicoId(),
                reservaCitaRequest.getFranjaHorariaId());
        return ResponseEntity.status(HttpStatus.CREATED).body(citaWebMapper.toDto(cita));
    }
}
