---

description: "Task list template for feature implementation"
---

# Tasks: Reserva de Cita en Línea 24/7

**Input**: Design documents from `/specs/001-reserva-cita-online/`

**Prerequisites**: plan.md (required), spec.md (required for user stories), research.md, data-model.md, contracts/, quickstart.md

**Tests**: La constitución del proyecto (Principio II, BDD) exige pruebas unitarias, de integración y
funcionales para todo el código, por lo que las tareas de prueba están incluidas explícitamente
(no son opcionales en este proyecto).

**Organization**: Las tareas se agrupan por historia de usuario para permitir implementación y prueba
independiente de cada una. `spec.md` define ambas historias con prioridad P1; se implementan en el
orden en que aparecen en la especificación (US1 primero, US2 segundo), ya que US2 depende de la
infraestructura de persistencia creada por US1 para ejercer el conflicto de doble reserva.

> **Nota de revisión (`/speckit-analyze`)**: Esta versión incorpora las correcciones de los hallazgos
> CRITICAL/HIGH C1, U1 e I1 (ver historial de análisis): (a) T005 configura explícitamente el esquema de
> base de datos H2, antes ausente; (b) T010–T012 añaden acceso de solo lectura a `Paciente`/`Medico`
> (antes inexistente), necesario para FR-004 (WhatsApp) y para los códigos 404 documentados en
> `contracts/openapi.yaml`; (c) T021 (antes T018) referencia explícitamente esa validación de existencia.
>
> **Segunda revisión (`/speckit-analyze`)**: Se corrigen los hallazgos previamente abiertos C1 (Coverage,
> anteriormente referenciado como "C2") y U1 (Underspecification, anteriormente "G1"): (a) T019a, T022 y
> T008 añaden el endpoint `GET /medicos` y `MedicoRepositoryPort.findAll()`, cerrando la brecha de
> descubrimiento de médicos para FR-002; (b) T018a añade la prueba unitaria del camino de fallo de
> notificación de FR-010, antes sin cobertura. El hallazgo de ambigüedad A1 (tipo de `id` de las
> entidades) se resolvió en `data-model.md`.
>
> **Tercera revisión (post-implementación)**: Se corrigen 4 brechas detectadas tras revisar el código
> ya implementado: (a) las pruebas de US2 ahora verifican el contenido del mensaje de invitación de
> FR-007, no solo el código `FRANJA_NO_DISPONIBLE`; (b) se añade `GET /citas?pacienteId&franjaHorariaId`
> (FR-011) + `ConsultarEstadoCitaUseCase`/`Service` + prueba de idempotencia, resolviendo el Edge Case de
> pérdida de conexión sin necesitar el `id` de cita que el paciente nunca recibió; (c) se documenta
> explícitamente en `spec.md`/`data-model.md` que "horario de atención" (FR-008) NO es un concepto
> separado de `FranjaHoraria`; (d) se modela `Medico.activo` (FR-012) y se valida en
> `ReservarCitaService`/`ConsultarFranjasDisponiblesService`/`ListarMedicosService`, con pruebas unitarias
> e de integración cubriendo el rechazo de médicos dados de baja.

## Format: `[ID] [P?] [Story] Description`

- **[P]**: Puede ejecutarse en paralelo (archivos distintos, sin dependencias)
- **[Story]**: A qué historia de usuario pertenece la tarea (US1, US2)
- Se incluyen rutas de archivo exactas en cada descripción

## Path Conventions

Proyecto único (Spring Boot, Clean Architecture por paquete) — ver `plan.md` § Project Structure:

- `src/main/java/com/mikels/citasalud/domain/...`
- `src/main/java/com/mikels/citasalud/application/...`
- `src/main/java/com/mikels/citasalud/infrastructure/...`
- `src/test/java/com/mikels/citasalud/{unit,integration,bdd}/...`
- `src/test/resources/features/...`

---

## Phase 1: Setup (Shared Infrastructure)

**Purpose**: Inicialización del proyecto, herramientas de build y esquema de base de datos requeridos por la constitución

- [X] T001 Agregar dependencias en `build.gradle`: `io.cucumber:cucumber-java`, `io.cucumber:cucumber-junit-platform-engine`, `org.junit.platform:junit-platform-suite` (para el runner de Cucumber)
- [X] T002 Agregar y configurar el plugin `org.openapitools.openapi-generator` en `build.gradle`, apuntando a `specs/001-reserva-cita-online/contracts/openapi.yaml`, generador `spring`, `interfaceOnly=true`, salida en `build/generated/openapi`
- [X] T003 [P] Agregar y configurar el plugin `jacoco` en `build.gradle` con `jacocoTestCoverageVerification` (regla `CLASS` mínimo 0.80, regla `BUNDLE` mínimo 0.80) enlazado a la tarea `check`
- [X] T004 [P] Crear estructura base de paquetes vacíos `domain/model`, `domain/exception`, `application/port/in`, `application/port/out`, `application/service`, `infrastructure/persistence/entity`, `infrastructure/persistence/repository`, `infrastructure/persistence/adapter`, `infrastructure/web/controller`, `infrastructure/web/mapper`, `infrastructure/notification`, `infrastructure/config` bajo `src/main/java/com/mikels/citasalud/`
- [X] T005 [P] Configurar el datasource H2 y la carga del esquema en `src/main/resources/application.yaml`: `spring.datasource` (H2 en memoria), `spring.jpa.hibernate.ddl-auto=update` (el esquema se genera a partir de las entidades JPA de T009/T010, evitando duplicar el DDL a mano), y `spring.jpa.defer-datasource-initialization=true` + `spring.sql.init.mode=always` para que `data.sql` (T013) se ejecute **después** de que Hibernate cree las tablas

**Checkpoint**: Build configurado; esquema de base de datos listo para poblarse; estructura de paquetes lista para el Phase 2

---

## Phase 2: Foundational (Blocking Prerequisites)

**Purpose**: Modelo de dominio, puertos, persistencia (incluyendo acceso de solo lectura a Paciente/Médico) y manejo de errores compartidos por ambas historias

**⚠️ CRITICAL**: Ninguna historia de usuario puede implementarse hasta completar esta fase

- [X] T006 Crear modelos de dominio `Paciente` (proyección mínima: `id`, `nombre`, `numeroWhatsApp`), `Medico` (proyección mínima: `id`, `nombre`, `especialidad`), `FranjaHoraria` (con enum `EstadoFranja { DISPONIBLE, OCUPADA }`), `Cita` (con enum `EstadoCita { CONFIRMADA }`) como POJOs sin anotaciones JPA en `src/main/java/com/mikels/citasalud/domain/model/`
- [X] T007 [P] Crear excepciones de dominio `FranjaNoDisponibleException` y `RecursoNoEncontradoException` en `src/main/java/com/mikels/citasalud/domain/exception/`
- [X] T008 Definir puertos de salida `CitaRepositoryPort`, `FranjaHorariaRepositoryPort`, `NotificationPort` (con método `enviarConfirmacion(Cita): NotificationResult`), `PacienteRepositoryPort` (`findById`, solo lectura), `MedicoRepositoryPort` (`findById` y `findAll`, solo lectura) en `src/main/java/com/mikels/citasalud/application/port/out/`
- [X] T009 [P] Crear entidades JPA `FranjaHorariaJpaEntity` (con `@Version` y restricción única `(medicoId, fecha, horaInicio)`) y `CitaJpaEntity` (con restricción única `(medicoId, fecha, horaInicio)`) en `src/main/java/com/mikels/citasalud/infrastructure/persistence/entity/`; usar Lombok (`@Getter`/`@Setter`/`@Builder`, evitando `@Data`) para el boilerplate
- [X] T010 [P] Crear entidades JPA mínimas de solo lectura `PacienteJpaEntity` (`id`, `nombre`, `numeroWhatsApp`) y `MedicoJpaEntity` (`id`, `nombre`, `especialidad`) en `src/main/java/com/mikels/citasalud/infrastructure/persistence/entity/` — respaldan las mismas tablas que una feature futura de gestión de pacientes/médicos poblará por completo (ver research.md §6)
- [X] T011 Crear repositorios Spring Data JPA `FranjaHorariaJpaRepository`, `CitaJpaRepository`, `PacienteJpaRepository` y `MedicoJpaRepository` en `src/main/java/com/mikels/citasalud/infrastructure/persistence/repository/`
- [X] T012 Implementar adaptadores `CitaRepositoryAdapter`, `FranjaHorariaRepositoryAdapter`, `PacienteRepositoryAdapter` y `MedicoRepositoryAdapter` que implementan los puertos de salida (T008) usando los repositorios de T011, en `src/main/java/com/mikels/citasalud/infrastructure/persistence/adapter/`
- [X] T013 [P] Crear datos semilla (`src/main/resources/db/data.sql`) con un médico de ejemplo, al menos un paciente de ejemplo (con `numeroWhatsApp` válido) y al menos 3 franjas horarias `DISPONIBLE` para ese médico, para desarrollo/test manual (ver `quickstart.md`)
- [X] T014 Configurar manejo global de excepciones (`@RestControllerAdvice`) mapeando `FranjaNoDisponibleException` → HTTP 409 (`codigo: FRANJA_NO_DISPONIBLE`) y `RecursoNoEncontradoException` → HTTP 404, en `src/main/java/com/mikels/citasalud/infrastructure/config/`

**Checkpoint**: Fundamentos listos (incluyendo esquema de BD cargado y acceso de solo lectura a Paciente/Médico) — las historias de usuario pueden implementarse

---

## Phase 3: User Story 1 - Reservar cita disponible fuera de horario telefónico (Priority: P1) 🎯 MVP

**Goal**: El paciente puede consultar franjas disponibles y confirmar una reserva en cualquier
momento; la cita queda registrada y se intenta la confirmación por WhatsApp.

**Independent Test**: Consultar `GET /medicos/{id}/franjas-disponibles`, confirmar una franja vía
`POST /citas`, y verificar que la cita queda persistida con `estado: CONFIRMADA` y que se generó un
intento de notificación (ver Escenario 1 de `quickstart.md`).

### Tests for User Story 1

- [X] T015 [P] [US1] Feature Gherkin de reserva exitosa (derivado del Acceptance Scenario 1 de `spec.md`) en `src/test/resources/features/reserva_cita_exitosa.feature`
- [X] T016 [P] [US1] Unit test de `ReservarCitaService` (camino feliz, mockeando `CitaRepositoryPort`, `FranjaHorariaRepositoryPort`, `PacienteRepositoryPort`, `MedicoRepositoryPort`, `NotificationPort`) en `src/test/java/com/mikels/citasalud/unit/application/ReservarCitaServiceTest.java`
- [X] T017 [P] [US1] Integration test de persistencia (`@DataJpaTest` con H2) verificando que al confirmar una cita la franja pasa a `OCUPADA` en `src/test/java/com/mikels/citasalud/integration/persistence/CitaPersistenceIT.java`
- [X] T018 [US1] Step definitions de Cucumber para el feature de T015, ejecutando contra `MockMvc`/`@SpringBootTest`, en `src/test/java/com/mikels/citasalud/bdd/ReservaCitaExitosaSteps.java`
- [X] T018a [P] [US1] Unit test de `ReservarCitaService` para el camino de fallo de notificación (mockear `NotificationPort.enviarConfirmacion` para retornar un resultado de fallo) verificando que la `Cita` se persiste igualmente con `notificacionEnviada=false` y que no se propaga ninguna excepción (FR-010), en `src/test/java/com/mikels/citasalud/unit/application/ReservarCitaServiceNotificacionFallidaTest.java`

### Implementation for User Story 1

- [X] T019 [P] [US1] Definir e implementar `ConsultarFranjasDisponiblesUseCase` (puerto de entrada) y `ConsultarFranjasDisponiblesService` en `src/main/java/com/mikels/citasalud/application/port/in/` y `application/service/`
- [X] T019a [P] [US1] Definir e implementar `ListarMedicosUseCase` (puerto de entrada) y `ListarMedicosService` (delega en `MedicoRepositoryPort.findAll()`) en `src/main/java/com/mikels/citasalud/application/port/in/` y `application/service/`
- [X] T020 [US1] Definir e implementar `ReservarCitaUseCase` (puerto de entrada) y `ReservarCitaService`: valida (vía `PacienteRepositoryPort`/`MedicoRepositoryPort`) que `pacienteId`/`medicoId` existan (si no, `RecursoNoEncontradoException` → 404), valida disponibilidad de la franja, persiste la `Cita`, marca la `FranjaHoraria` como `OCUPADA` en la misma transacción, obtiene el `numeroWhatsApp` del paciente e invoca `NotificationPort` en `src/main/java/com/mikels/citasalud/application/port/in/` y `application/service/`
- [X] T021 [P] [US1] Implementar `WhatsAppNotificationAdapter` (implementa `NotificationPort`, cliente REST hacia WhatsApp Business Cloud API, retorna resultado de fallo sin lanzar excepción si el envío no puede completarse) en `src/main/java/com/mikels/citasalud/infrastructure/notification/`
- [X] T022 [US1] Implementar controlador REST que implementa las interfaces generadas por `openapi-generator` para `GET /medicos`, `GET /medicos/{medicoId}/franjas-disponibles` y `POST /citas` en `src/main/java/com/mikels/citasalud/infrastructure/web/controller/`
- [X] T023 [US1] Implementar mappers DTO (generado) ⇄ modelo de dominio en `src/main/java/com/mikels/citasalud/infrastructure/web/mapper/`

**Checkpoint**: User Story 1 completamente funcional y probable de forma independiente (MVP)

---

## Phase 4: User Story 2 - Evitar reservar una franja ya ocupada (Priority: P1)

**Goal**: El sistema rechaza la confirmación de una franja ya ocupada (incluyendo bajo intentos
concurrentes) y muestra al paciente que debe elegir otra franja.

**Independent Test**: Reservar una franja, luego intentar reservarla de nuevo con otro paciente
(debe recibir HTTP 409); disparar dos solicitudes concurrentes sobre la misma franja y verificar que
solo una resulta en una `Cita` `CONFIRMADA` (ver Escenarios 2 y 3 de `quickstart.md`).

### Tests for User Story 2

- [X] T024 [P] [US2] Feature Gherkin de franja ya ocupada (derivado del Acceptance Scenario 1 de US2 en `spec.md`) en `src/test/resources/features/franja_ya_ocupada.feature`
- [X] T025 [P] [US2] Unit test de `ReservarCitaService` verificando que lanza `FranjaNoDisponibleException` cuando la franja no está `DISPONIBLE` en `src/test/java/com/mikels/citasalud/unit/application/ReservarCitaServiceConflictoTest.java`
- [X] T026 [P] [US2] Integration test (`@SpringBootTest` con H2) que ejecuta dos inserciones concurrentes de `Cita` sobre la misma franja y verifica que la restricción única de base de datos garantiza que solo una tenga éxito, en `src/test/java/com/mikels/citasalud/integration/persistence/ReservaConcurrenteIT.java`
- [X] T027 [US2] Step definitions de Cucumber para el feature de T024 en `src/test/java/com/mikels/citasalud/bdd/FranjaYaOcupadaSteps.java`

### Implementation for User Story 2

- [X] T028 [US2] Completar `ReservarCitaService` (de T020) para capturar `DataIntegrityViolationException` ante inserciones concurrentes y traducirla a `FranjaNoDisponibleException`, en `src/main/java/com/mikels/citasalud/application/service/ReservarCitaService.java`
- [X] T029 [US2] Verificar/extender el `@RestControllerAdvice` de T014 para asegurar que `FranjaNoDisponibleException` siempre responde HTTP 409 con el cuerpo `Error` (`codigo: FRANJA_NO_DISPONIBLE`) definido en `contracts/openapi.yaml`, en `src/main/java/com/mikels/citasalud/infrastructure/config/`

**Checkpoint**: Ambas historias de usuario funcionan de forma independiente y en conjunto

---

## Phase 5: Polish & Cross-Cutting Concerns

**Purpose**: Verificación final de cumplimiento de la constitución y de la guía de validación

- [ ] T030 Ejecutar manualmente los 3 escenarios de `quickstart.md` contra la aplicación levantada con `./gradlew bootRun` y confirmar los resultados esperados (incluye verificar que el esquema H2 se creó correctamente y los datos semilla de T013 cargaron)
- [ ] T031 [P] Ejecutar `./gradlew jacocoTestReport` y `./gradlew jacocoTestCoverageVerification`; ajustar pruebas donde falte cobertura hasta cumplir >80% por clase y ≥80% global
- [X] T032 Revisión de cumplimiento SOLID/YAGNI/DRY y uso correcto de Lombok sobre las clases nuevas de `domain/`, `application/` e `infrastructure/` (Principios III y Development Standards de la constitución)
- [X] T033 [P] Actualizar `README.md` (o crear uno si no existe) con instrucciones básicas de arranque enlazando a `specs/001-reserva-cita-online/quickstart.md`

---

## Dependencies & Execution Order

### Phase Dependencies

- **Setup (Phase 1)**: Sin dependencias — puede iniciar de inmediato
- **Foundational (Phase 2)**: Depende de Setup (en particular, T005 debe existir antes de que T013 pueda cargar datos semilla) — BLOQUEA ambas historias de usuario
- **User Story 1 (Phase 3)**: Depende de Foundational — MVP, puede entregarse sola
- **User Story 2 (Phase 4)**: Depende de Foundational; reutiliza el flujo de `ReservarCitaService` creado en US1 (T020) para añadir el manejo de conflicto — no puede probarse de forma significativa sin que US1 exista, pero no modifica el contrato ni el comportamiento de US1 en el camino feliz
- **Polish (Phase 5)**: Depende de que ambas historias estén completas

### Within Each User Story

- Tests (T015–T018, T024–T027) se escriben y deben FALLAR antes de la implementación correspondiente
- Modelos/puertos (Foundational) antes que servicios de aplicación
- Servicios de aplicación antes que controladores REST
- Implementación core antes que integración con notificación externa

### Parallel Opportunities

- T003 y T004 (Setup) en paralelo; T005 puede ir en paralelo con T003/T004 (archivo distinto)
- T007, T009, T010, T013 (Foundational) en paralelo entre sí (archivos distintos)
- Dentro de US1: T015, T016, T017, T018a en paralelo; T019, T019a y T021 en paralelo
- Dentro de US2: T024, T025, T026 en paralelo
- T031 y T033 (Polish) en paralelo

---

## Parallel Example: User Story 1

```bash
# Lanzar en paralelo las pruebas de User Story 1:
Task: "Feature Gherkin de reserva exitosa en src/test/resources/features/reserva_cita_exitosa.feature"
Task: "Unit test de ReservarCitaService en src/test/java/.../unit/application/ReservarCitaServiceTest.java"
Task: "Integration test de persistencia en src/test/java/.../integration/persistence/CitaPersistenceIT.java"

# Lanzar en paralelo implementación independiente dentro de US1:
Task: "Implementar ConsultarFranjasDisponiblesUseCase/Service"
Task: "Implementar WhatsAppNotificationAdapter"
```

---

## Implementation Strategy

### MVP First (User Story 1 solamente)

1. Completar Phase 1: Setup (incluye esquema de BD)
2. Completar Phase 2: Foundational (bloquea todo lo demás)
3. Completar Phase 3: User Story 1
4. **DETENER y VALIDAR**: probar User Story 1 de forma independiente (Escenario 1 de `quickstart.md`)
5. Desplegar/demostrar si está listo

### Incremental Delivery

1. Setup + Foundational → base lista (esquema cargado, Paciente/Médico legibles)
2. Agregar User Story 1 → probar independientemente → Demo (MVP)
3. Agregar User Story 2 → probar independientemente → Demo
4. Phase 5 (Polish) → verificación de cobertura y cumplimiento de constitución

---

## Notes

- [P] = archivos distintos, sin dependencias entre sí
- [Story] mapea cada tarea a su historia de usuario para trazabilidad
- Verificar que las pruebas fallen antes de implementar (Red-Green-Refactor, Principio II)
- Detenerse en cada checkpoint para validar la historia de forma independiente
- Evitar: tareas vagas, conflictos de mismo archivo, dependencias cruzadas entre historias que rompan la independencia
- Hallazgos de `/speckit-analyze` aún abiertos (no bloqueantes, quedan para una iteración futura):
  G2 (sin prueba de latencia para SC-002), D2 (sin tarea de CheckStyle). Los hallazgos C2 (endpoint de
  listado de médicos) y G1 (prueba del camino de fallo de notificación FR-010) fueron corregidos — ver
  T008, T018a, T019a, T022.
