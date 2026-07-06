# Data Model: Reserva de Cita en Línea 24/7

**Decisión de tipo de identificador**: Todos los `id` de las entidades siguientes se implementan como
`UUID` nativo (columna `UUID` en H2, tipo Java `java.util.UUID`), generado por la aplicación
(`UUID.randomUUID()`) antes de persistir. Esto coincide directamente con `format: uuid` en
`contracts/openapi.yaml`, evitando una capa de traducción Long↔UUID en los mappers (T023).

## Entidades

### Paciente

Persona que solicita y reserva la cita.

| Campo             | Tipo      | Reglas de validación                                              |
|--------------------|-----------|---------------------------------------------------------------------|
| id                 | UUID      | Identificador único, generado por la aplicación                      |
| nombre             | String    | Obligatorio, no vacío                                                |
| numeroWhatsApp     | String    | Obligatorio; formato E.164 (ej. +573001234567); usado por FR-004    |

*Nota*: La creación, actualización y autenticación del paciente están fuera de alcance (ver Assumptions
en spec.md). Sin embargo, esta feature SÍ requiere acceso de **solo lectura** a `id`, `nombre` y
`numeroWhatsApp` para poder: (a) validar que el `pacienteId` recibido exista (HTTP 404, ver
`contracts/openapi.yaml`), y (b) obtener el número al que enviar la confirmación por WhatsApp (FR-004).
Por lo tanto, esta feature SÍ define una entidad JPA mínima de solo lectura (`PacienteJpaEntity`) y un
puerto `PacienteRepositoryPort` (ver Foundational en `tasks.md`); la gestión completa (alta, edición,
autenticación) queda para una feature futura sobre la misma tabla.

### Médico

Profesional de salud que atiende citas.

| Campo             | Tipo      | Reglas de validación                          |
|--------------------|-----------|-------------------------------------------------|
| id                 | UUID      | Identificador único, generado por la aplicación  |
| nombre             | String    | Obligatorio, no vacío                            |
| especialidad       | String    | Obligatorio                                      |

*Nota*: La gestión de médicos y su agenda base está fuera de alcance (ver Assumptions). Sin embargo,
esta feature SÍ requiere acceso de **solo lectura** a `id`, `nombre` y `especialidad`, tanto para
validar que el `medicoId` recibido exista (HTTP 404, ver `contracts/openapi.yaml`) como para listar
los médicos disponibles y permitir que el paciente elija uno (FR-002, `GET /medicos`). Por lo tanto,
esta feature SÍ define una entidad JPA mínima de solo lectura (`MedicoJpaEntity`) y un puerto
`MedicoRepositoryPort` con `findById` y `findAll` (ver Foundational en `tasks.md`); la gestión completa
(alta, edición de agenda) queda para una feature futura sobre la misma tabla.

### FranjaHoraria (Disponibilidad)

Bloque de tiempo asociado a un médico que puede estar disponible u ocupado. Representa la agenda
pre-configurada de cada médico (FR-008).

| Campo             | Tipo          | Reglas de validación                                                        |
|--------------------|---------------|--------------------------------------------------------------------------------|
| id                 | UUID          | Identificador único, generado por la aplicación                                 |
| medicoId           | FK → Médico   | Obligatorio                                                                     |
| fecha              | LocalDate     | Obligatorio                                                                     |
| horaInicio         | LocalTime     | Obligatorio                                                                     |
| horaFin            | LocalTime     | Obligatorio; debe ser posterior a `horaInicio`                                  |
| estado             | Enum          | `DISPONIBLE` \| `OCUPADA`; por defecto `DISPONIBLE`                            |
| version            | Long          | Bloqueo optimista (`@Version`), defensa adicional contra condiciones de carrera |

**Restricción de unicidad**: `(medicoId, fecha, horaInicio)` — no pueden existir dos franjas idénticas
para el mismo médico (garantiza FR-008/FR-002 a nivel de agenda).

**Transición de estado**: `DISPONIBLE → OCUPADA` ocurre exclusivamente al confirmarse una `Cita` sobre esa
franja, dentro de la misma transacción (ver research.md §1). No existe transición inversa en el alcance de
esta historia (cancelaciones fuera de alcance).

### Cita

Reserva confirmada entre un paciente y un médico en una franja horaria específica.

| Campo               | Tipo               | Reglas de validación                                                     |
|----------------------|--------------------|-----------------------------------------------------------------------------|
| id                   | UUID               | Identificador único, generado por la aplicación                             |
| pacienteId           | FK → Paciente      | Obligatorio                                                                  |
| medicoId             | FK → Médico        | Obligatorio                                                                  |
| franjaHorariaId      | FK → FranjaHoraria | Obligatorio; debe referenciar una franja en estado `DISPONIBLE` al momento de crear la cita |
| fecha                | LocalDate          | Obligatorio; debe coincidir con la fecha de la franja referenciada          |
| horaInicio           | LocalTime          | Obligatorio; debe coincidir con la hora de inicio de la franja referenciada |
| estado               | Enum               | `CONFIRMADA` (único valor en el alcance de esta historia)                   |
| notificacionEnviada  | boolean            | Indica si el intento de notificación WhatsApp (FR-004) fue exitoso          |
| creadoEn             | Instant            | Timestamp de registro, asignado por el sistema                             |

**Restricción de unicidad**: `(medicoId, fecha, horaInicio)` — garantiza que no puedan existir dos citas
`CONFIRMADA` para el mismo médico en la misma franja (FR-006, FR-009; ver research.md §1).

**Relaciones**:
- `Cita` → `Paciente` (muchos a uno)
- `Cita` → `Médico` (muchos a uno)
- `Cita` → `FranjaHoraria` (uno a uno, la franja queda consumida por la cita)

## Reglas de validación derivadas de los requisitos

- FR-002/FR-008: solo se pueden listar/seleccionar `FranjaHoraria` con `estado = DISPONIBLE` y dentro de la
  agenda del médico.
- FR-005/FR-006/FR-009: la creación de `Cita` DEBE re-validar `estado = DISPONIBLE` de la franja dentro de
  la misma transacción que la inserción; cualquier violación de la restricción única se traduce a
  `FranjaNoDisponibleException` (HTTP 409).
- FR-003: la persistencia de `Cita` es independiente del resultado de la notificación; `notificacionEnviada`
  se actualiza después, sin revertir la transacción de creación si la notificación falla.
- FR-010: si `notificacionEnviada = false`, la respuesta al paciente incluye igualmente los datos de la cita
  confirmada (medio alterno visible en pantalla).
- Validación de existencia: antes de crear la `Cita`, el servicio DEBE validar (vía `PacienteRepositoryPort`
  y `MedicoRepositoryPort`) que `pacienteId` y `medicoId` existan; de lo contrario se lanza
  `RecursoNoEncontradoException` (HTTP 404), cumpliendo las respuestas 404 documentadas en
  `contracts/openapi.yaml`.
