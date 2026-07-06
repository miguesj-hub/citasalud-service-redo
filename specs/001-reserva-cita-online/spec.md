# Feature Specification: Reserva de Cita en Línea 24/7

**Feature Branch**: `001-reserva-cita-online`

**Created**: 2026-07-05

**Status**: Draft

**Input**: User description: "US-01 · Reserva de cita en linea 24/7 · epica E-01 · 8 pts. Como paciente, quiero reservar una cita en linea en cualquier momento del dia, para no tener que llamar durante mi horario de almuerzo ni acumular intentos fallidos. Criterios de aceptacion (Gherkin): Dado que el paciente accede al sistema fuera del horario de atencion telefonica, cuando elige medico, fecha y hora disponibles y confirma, entonces la cita queda registrada y el paciente recibe confirmacion por WhatsApp. Dado que el paciente intenta seleccionar una franja ya ocupada, cuando intenta confirmarla, entonces el sistema la muestra como no disponible y lo invita a elegir otra franja."

## User Scenarios & Testing *(mandatory)*

### User Story 1 - Reservar cita disponible fuera de horario telefónico (Priority: P1)

Como paciente, quiero reservar una cita en línea en cualquier momento del día (incluyendo fuera del horario de atención telefónica), eligiendo médico, fecha y hora disponibles, para no depender de llamar durante mi horario de almuerzo ni acumular intentos fallidos.

**Why this priority**: Es el flujo principal de la funcionalidad; sin esta capacidad no existe el producto. Resuelve directamente el dolor del paciente (no poder llamar en horario laboral) y es la única historia que por sí sola entrega valor de negocio completo (MVP).

**Independent Test**: Puede probarse completamente accediendo al sistema fuera del horario de atención telefónica (por ejemplo, a las 22:00), seleccionando un médico, una fecha y una franja horaria disponible, confirmando la reserva, y verificando que la cita queda registrada en el sistema y que el paciente recibe una confirmación por WhatsApp.

**Acceptance Scenarios**:

1. **Given** el paciente accede al sistema fuera del horario de atención telefónica, **When** elige médico, fecha y hora disponibles y confirma, **Then** la cita queda registrada y el paciente recibe confirmación por WhatsApp.
2. **Given** el paciente accede al sistema dentro del horario de atención telefónica, **When** elige médico, fecha y hora disponibles y confirma, **Then** la cita queda registrada de la misma manera (el sistema está disponible 24/7 sin distinción de horario).

---

### User Story 2 - Evitar reservar una franja ya ocupada (Priority: P1)

Como paciente, quiero que el sistema me impida confirmar una franja horaria que ya fue tomada por otro paciente, para no acumular intentos fallidos ni generar conflictos de doble reserva con el médico.

**Why this priority**: Es una condición de integridad crítica del sistema de reservas. Sin esta validación, el sistema permitiría citas duplicadas, lo cual rompe la confianza del paciente y genera conflictos operativos para la clínica. Tiene la misma prioridad que la Historia 1 porque ambas son necesarias para que el flujo de reserva sea confiable desde el primer lanzamiento.

**Independent Test**: Puede probarse de forma independiente simulando dos intentos de reserva sobre la misma franja horaria (médico, fecha y hora idénticos) y verificando que el segundo intento es rechazado con un mensaje de franja no disponible, mientras el primero se confirma exitosamente.

**Acceptance Scenarios**:

1. **Given** el paciente intenta seleccionar una franja ya ocupada, **When** intenta confirmarla, **Then** el sistema la muestra como no disponible y lo invita a elegir otra franja.
2. **Given** dos pacientes visualizan la misma franja disponible al mismo tiempo, **When** ambos intentan confirmarla casi simultáneamente, **Then** solo el primero en completar la confirmación obtiene la cita y el segundo recibe el aviso de franja no disponible.

---

### Edge Cases

- ¿Qué sucede si el paciente pierde la conexión a internet justo después de confirmar pero antes de recibir la respuesta del sistema? El sistema no debe crear reservas duplicadas ni dejar la cita en un estado ambiguo; al reconectar, el paciente debe poder verificar el estado real de su cita.
- ¿Qué sucede si el número de WhatsApp del paciente es inválido o no puede recibir el mensaje? La cita debe quedar registrada igualmente y el sistema debe ofrecer un medio alterno de confirmación (visible en pantalla) para que el paciente no dependa únicamente de WhatsApp.
- ¿Qué sucede si el paciente selecciona una franja disponible pero el médico es dado de baja o su agenda es modificada antes de confirmar? El sistema debe re-validar la disponibilidad en el momento de la confirmación, no solo en el momento de la selección.
- ¿Qué sucede si el paciente intenta reservar una fecha u hora fuera del horario de atención del médico seleccionado? El sistema no debe mostrar esas franjas como seleccionables.

## Requirements *(mandatory)*

### Functional Requirements

- **FR-001**: El sistema DEBE permitir a los pacientes reservar una cita en línea en cualquier momento (24 horas al día, 7 días a la semana), sin restricción de horario de atención telefónica.
- **FR-002**: El sistema DEBE permitir al paciente seleccionar médico, fecha y hora entre las franjas disponibles antes de confirmar la reserva.
- **FR-003**: El sistema DEBE registrar la cita de forma persistente en cuanto el paciente confirme una franja disponible.
- **FR-004**: El sistema DEBE enviar una confirmación de la cita al paciente vía WhatsApp inmediatamente después de que la reserva quede registrada.
- **FR-005**: El sistema DEBE validar la disponibilidad real de la franja seleccionada en el momento de la confirmación (no solo al momento de mostrarla), para evitar condiciones de carrera entre pacientes concurrentes.
- **FR-006**: El sistema DEBE rechazar la confirmación de una franja horaria que ya esté ocupada, mostrando al paciente que la franja no está disponible.
- **FR-007**: El sistema DEBE invitar al paciente a elegir otra franja disponible cuando la seleccionada ya no esté disponible.
- **FR-008**: El sistema DEBE mostrar únicamente franjas horarias que estén dentro del horario de atención del médico seleccionado.
- **FR-009**: El sistema DEBE garantizar que ante intentos de confirmación simultáneos sobre la misma franja, únicamente uno resulte en una cita registrada.
- **FR-010**: El sistema DEBE ofrecer un medio alterno de confirmación visible en pantalla cuando el envío de la confirmación por WhatsApp no pueda completarse, sin que esto impida el registro de la cita.

### Key Entities

- **Paciente**: Persona que solicita y reserva la cita. Atributos clave: identificador, nombre, número de contacto (WhatsApp).
- **Médico**: Profesional de salud que atiende citas. Atributos clave: identificador, nombre, especialidad, horario de atención/disponibilidad.
- **Cita**: Reserva confirmada entre un paciente y un médico en una franja horaria específica. Atributos clave: identificador, paciente asociado, médico asociado, fecha, hora, estado (confirmada, no disponible/rechazada).
- **Franja Horaria (Disponibilidad)**: Bloque de tiempo asociado a un médico que puede estar disponible u ocupado. Atributos clave: médico asociado, fecha, hora de inicio/fin, estado de disponibilidad.

## Success Criteria *(mandatory)*

### Measurable Outcomes

- **SC-001**: Los pacientes pueden completar una reserva de cita (selección de médico, fecha, hora y confirmación) en menos de 3 minutos.
- **SC-002**: El 100% de las citas confirmadas generan un intento de notificación por WhatsApp en menos de 1 minuto después del registro.
- **SC-003**: El sistema está disponible para reservas el 100% de las horas del día, sin ventanas de bloqueo relacionadas con horario de atención telefónica.
- **SC-004**: 0% de citas dobles (dos citas confirmadas para el mismo médico en la misma franja horaria) se producen bajo uso normal o concurrente del sistema.
- **SC-005**: El 90% de los pacientes que intentan reservar una franja ya ocupada logran completar una reserva alternativa en el mismo intento de sesión, sin abandonar el flujo.

## Assumptions

- El paciente ya está identificado/autenticado en el sistema antes de iniciar el flujo de reserva (el mecanismo de autenticación está fuera del alcance de esta historia).
- Cada paciente cuenta con un número de WhatsApp válido registrado en su perfil; la validación y actualización de este dato se asume gestionada en otro flujo del sistema.
- Los médicos y sus horarios/disponibilidad ya existen previamente configurados en el sistema; la creación y mantenimiento de agendas médicas está fuera del alcance de esta historia.
- "Horario de atención telefónica" se refiere a un horario comercial estándar (por ejemplo, mañana y tarde en días hábiles); el sistema de reserva en línea no tiene restricciones de horario independientemente de este dato.
- Solo se permite una cita por franja horaria por médico (no se contempla en esta historia la posibilidad de citas simultáneas o múltiples pacientes por franja).
