# language: es
Característica: Reserva de cita en línea 24/7

  Escenario: Reserva exitosa fuera de horario telefónico
    Dado que el paciente accede al sistema fuera del horario de atención telefónica
    Cuando elige un médico, una fecha y una hora disponibles y confirma la reserva
    Entonces la cita queda registrada con estado "CONFIRMADA"
    Y el paciente recibe un intento de confirmación por WhatsApp
