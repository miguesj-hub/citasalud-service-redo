package com.mikels.citasalud.infrastructure.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import com.mikels.citasalud.domain.exception.FranjaNoDisponibleException;
import com.mikels.citasalud.domain.exception.RecursoNoEncontradoException;
import com.mikels.citasalud.infrastructure.web.generated.model.Error;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FranjaNoDisponibleException.class)
    public ResponseEntity<Error> manejarFranjaNoDisponible(FranjaNoDisponibleException ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(construirError("FRANJA_NO_DISPONIBLE", ex.getMessage()));
    }

    @ExceptionHandler(RecursoNoEncontradoException.class)
    public ResponseEntity<Error> manejarRecursoNoEncontrado(RecursoNoEncontradoException ex) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(construirError("RECURSO_NO_ENCONTRADO", ex.getMessage()));
    }

    private Error construirError(String codigo, String mensaje) {
        Error error = new Error();
        error.setCodigo(codigo);
        error.setMensaje(mensaje);
        return error;
    }
}
