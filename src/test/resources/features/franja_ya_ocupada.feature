# language: es
Característica: Prevención de doble reserva de una franja horaria

  Escenario: El paciente intenta reservar una franja ya ocupada
    Dado que existe una franja horaria ya reservada por otro paciente
    Cuando el paciente intenta confirmar esa misma franja
    Entonces el sistema responde que la franja no está disponible
    Y no se registra una segunda cita para esa franja
