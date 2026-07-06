# Research: Reserva de Cita en Línea 24/7

## 1. Prevención de doble reserva (concurrencia)

**Decision**: Aplicar una restricción de unicidad a nivel de base de datos sobre `(medico_id, fecha, hora_inicio)`
en la tabla de citas, combinada con un método de servicio transaccional que capture
`DataIntegrityViolationException` y la traduzca a una excepción de dominio `FranjaNoDisponibleException`
(mapeada a HTTP 409 Conflict en el adaptador web). Adicionalmente, la entidad `FranjaHoraria` lleva un
campo `@Version` (bloqueo optimista de JPA) como segunda barrera de defensa al actualizar su estado a
OCUPADA dentro de la misma transacción de creación de la cita.

**Rationale**: La restricción única en base de datos es la única garantía que sigue siendo válida incluso
si el proceso de aplicación se escala horizontalmente (múltiples instancias) o si existe un error de
lógica en el servicio; el índice único hace que la condición de carrera sea imposible de violar a nivel de
almacenamiento, cumpliendo FR-005, FR-006 y FR-009 sin necesitar infraestructura de bloqueo distribuido.

**Alternatives considered**:
- *Bloqueo pesimista (`SELECT ... FOR UPDATE`)*: descartado como mecanismo único porque añade contención y
  complejidad de despliegue sin aportar más garantías que una restricción única, violando YAGNI.
- *Solo bloqueo optimista (`@Version`) sin restricción única*: descartado porque no protege ante inserciones
  concurrentes de nuevas filas (el bloqueo optimista solo protege actualizaciones de una fila existente).
- *Cola de mensajes para serializar reservas*: descartado por sobre-ingeniería para el volumen esperado de
  una clínica (ver Scale/Scope en el plan).

## 2. Integración de confirmación por WhatsApp

**Decision**: Definir un puerto de salida `NotificationPort` (`enviarConfirmacion(Cita): NotificationResult`)
en la capa de aplicación, implementado por un adaptador de infraestructura que invoca la WhatsApp Business
Cloud API (Meta) mediante un cliente REST. El caso de uso de reserva persiste la cita de forma
independiente del resultado de la notificación (FR-003 no depende de FR-004); si el envío falla, el
adaptador retorna un resultado de fallo y la respuesta de confirmación expuesta al paciente incluye un
medio alterno visible en pantalla (FR-010).

**Rationale**: Aislar la notificación detrás de un puerto respeta el principio de Inversión de Dependencias
(Clean Architecture / SOLID): la capa de aplicación no conoce el proveedor concreto de mensajería, lo que
permite sustituirlo (Twilio, proveedor propio, etc.) sin tocar la lógica de negocio. No acoplar el registro
de la cita al éxito de la notificación cumple FR-010 y evita que un fallo de un servicio externo bloquee el
flujo crítico de negocio.

**Alternatives considered**:
- *Twilio WhatsApp API*: alternativa igualmente válida; queda como implementación intercambiable detrás del
  mismo puerto, no se descarta, se documenta como opción futura.
- *Mensajería asíncrona vía cola/broker con reintentos*: descartado para este alcance (YAGNI); SC-002 solo
  exige que se genere el intento de notificación en menos de 1 minuto, no una garantía de entrega
  reintentada indefinidamente.

## 3. Framework BDD para pruebas funcionales

**Decision**: Usar Cucumber-JVM (`io.cucumber:cucumber-java`, `io.cucumber:cucumber-junit-platform-engine`)
integrado sobre JUnit 5 Platform y Spring Boot Test. Los archivos `.feature` en Gherkin se ubican en
`src/test/resources/features/` y se derivan 1:1 de los escenarios Given/When/Then ya definidos en
`spec.md`.

**Rationale**: Cucumber-JVM es el estándar de facto para BDD en el ecosistema Java/Spring y permite mapear
directamente los criterios de aceptación en Gherkin de la especificación a step definitions ejecutables,
cumpliendo el Principio II (BDD) de la constitución sin reescribir los escenarios ya validados con el
negocio.

**Alternatives considered**:
- *JBehave*: descartado, menor mantenimiento activo y comunidad más reducida que Cucumber-JVM.
- *Spock (Groovy)*: descartado por introducir un segundo lenguaje (Groovy) en un stack puramente Java,
  incrementando la complejidad de build sin beneficio claro (viola YAGNI/DRY del stack).

## 4. Generación de API a partir de contrato OpenAPI

**Decision**: Usar el plugin `org.openapitools.openapi-generator` para Gradle, generando interfaces de
servidor Java (`spring` generator, `interfaceOnly=true`) a partir de `contracts/openapi.yaml` en tiempo de
build (`build/generated/openapi`). Los controladores de la capa de adaptadores implementan estas interfaces
generadas.

**Rationale**: Cumple el Principio IV (API First) de la constitución: el contrato OpenAPI es la fuente de
verdad y se escribe antes que el código; el generador evita divergencia manual entre el contrato y los
DTOs/firmas de los controladores.

**Alternatives considered**:
- *springdoc-openapi (code-first)*: genera el contrato A PARTIR del código (dirección inversa a la
  requerida por API-First); se descarta como mecanismo de generación del contrato, pero puede añadirse
  únicamente como UI de documentación (Swagger UI) sirviendo el contrato ya escrito, sin generar el
  contrato desde anotaciones.

## 5. Cobertura de pruebas con JaCoCo

**Decision**: Configurar el plugin `jacoco` de Gradle con reglas de verificación (`jacocoTestCoverageVerification`)
enlazadas a la tarea `check`: una regla `BUNDLE` con mínimo 0.80 de `INSTRUCTION`/`LINE` covered ratio a
nivel global, y una regla `CLASS` con mínimo 0.80 por clase, excluyendo clases generadas (DTOs/interfaces de
`build/generated/openapi`) y clases de configuración trivial anotadas explícitamente.

**Rationale**: Cumple el Principio V de la constitución (coverage por clase > 80%, coverage global >= 80%)
de forma automatizada y bloqueante en el build, evitando que el umbral dependa de revisión manual.

**Alternatives considered**:
- *SonarQube quality gate*: descartado para este alcance por requerir infraestructura adicional (servidor
  Sonar); puede añadirse en el futuro sin reemplazar el gate local de JaCoCo.

## 6. Acceso de solo lectura a Paciente y Médico

**Decision**: Aunque la creación/gestión de `Paciente` y `Médico` está fuera de alcance (ver Assumptions
en `spec.md`), esta feature define entidades JPA mínimas de solo lectura (`PacienteJpaEntity`,
`MedicoJpaEntity`) y sus puertos correspondientes (`PacienteRepositoryPort`, `MedicoRepositoryPort`),
respaldadas por las mismas tablas que una feature futura de gestión de pacientes/médicos poblará por
completo. `ReservarCitaService` usa estos puertos para (a) validar la existencia de `pacienteId`/`medicoId`
antes de reservar (HTTP 404) y (b) obtener `numeroWhatsApp` del paciente para la notificación (FR-004).

**Rationale**: Sin esta pieza, FR-004 (enviar confirmación por WhatsApp) y los códigos 404 documentados en
`contracts/openapi.yaml` no serían implementables — no existiría ningún mecanismo para leer el número de
contacto ni para distinguir un ID inexistente de una franja simplemente vacía. Modelar el acceso de solo
lectura (sin exponer endpoints de creación/edición) mantiene el alcance acotado a lo estrictamente
necesario para esta historia (YAGNI), evitando construir un CRUD completo de pacientes/médicos que
pertenece a otra feature.

**Alternatives considered**:
- *Cliente REST a un servicio externo de pacientes/médicos*: descartado por falta de evidencia de que tal
  servicio exista hoy; se prefiere leer directamente de la base de datos compartida (mismo esquema H2)
  hasta que se documente lo contrario.
- *No validar existencia y confiar solo en la restricción de clave foránea de la base de datos*: descartado
  porque una violación de FK no distingue fácilmente cuál de los tres IDs (`paciente`, `medico`, `franja`)
  falló, dificultando devolver el mensaje 404 correcto al paciente.

## 7. Médico dado de baja (FR-012)

**Decision**: Añadir un campo `activo` (boolean, por defecto `true`) a `MedicoJpaEntity`/`Medico`. Un
médico con `activo = false` se trata exactamente igual que un `medicoId` inexistente: `GET /medicos` lo
excluye del listado (`findByActivoTrue`), y tanto `ReservarCitaService` como
`ConsultarFranjasDisponiblesService` lanzan `RecursoNoEncontradoException` (HTTP 404) si el médico
encontrado por id no está activo.

**Rationale**: El Edge Case de `spec.md` ("¿Qué sucede si... el médico es dado de baja... antes de
confirmar?") exige re-validar en el momento de la confirmación, no solo al momento de la selección. Sin
un campo de estado, esa re-validación sería imposible de implementar. Tratar "dado de baja" igual que
"no encontrado" (en vez de un código de error distinto) evita filtrar información interna de gestión de
médicos al paciente, consistente con FR-012.

**Alternatives considered**:
- *Borrado físico del médico al darlo de baja*: descartado — rompería la integridad referencial de
  `Cita`/`FranjaHoraria` históricas.
- *Código de error HTTP/mensaje distinto para "médico dado de baja" vs "médico inexistente"*: descartado
  por ahora (YAGNI); el paciente no necesita distinguir ambos casos, y el spec pide tratarlo "como si no
  existiera".

## 8. Consulta de estado de cita (FR-011)

**Decision**: Añadir `GET /citas?pacienteId={id}&franjaHorariaId={id}` (idempotente, sin efectos
secundarios) que busca una `Cita` `CONFIRMADA` por esa combinación exacta y responde 200 con la cita si
existe, o 404 si no. Se identifica por `pacienteId` + `franjaHorariaId` —no por el `id` de la cita—
porque son los únicos datos que el paciente ya conoce **antes** de confirmar; si pierde la conexión justo
después de enviar la solicitud pero antes de recibir la respuesta (Edge Case de `spec.md`), nunca llegó a
ver el `id` generado por el servidor y por tanto no podría usarlo para verificar el resultado.

**Rationale**: Resuelve directamente el Edge Case de pérdida de conexión sin necesitar mecanismos más
complejos (tokens de idempotencia del lado del cliente, WebSockets, polling con reintentos exponenciales)
que estarían sobre-dimensionados (YAGNI) para el alcance de esta historia.

**Alternatives considered**:
- *Exigir un `Idempotency-Key` enviado por el cliente en el header de `POST /citas`*: patrón robusto y
  usado en APIs de pagos, pero requiere que el cliente genere y persista esa clave ANTES de la solicitud
  original — mismo problema de "qué hace el cliente si nunca generó/guardó nada" si la pérdida de conexión
  ocurre en el primer intento; además añade complejidad no solicitada por ninguna historia de usuario
  actual. Se documenta como evolución futura si se requiere reintentos automáticos del lado del cliente.
- *Consultar por `id` de cita*: descartado como único mecanismo por la razón explicada arriba (el
  paciente podría no conocerlo).
