# Implementation Plan: Reserva de Cita en Línea 24/7

**Branch**: `001-reserva-cita-online` | **Date**: 2026-07-05 | **Spec**: [spec.md](./spec.md)

**Input**: Feature specification from `/specs/001-reserva-cita-online/spec.md`

**Note**: This template is filled in by the `/speckit-plan` command. See `.specify/templates/plan-template.md` for the execution workflow.

## Summary

Permitir a los pacientes reservar una cita médica en línea en cualquier momento (24/7), eligiendo
médico, fecha y franja horaria disponible, con registro persistente inmediato y confirmación por
WhatsApp; y garantizar que ninguna franja pueda ser reservada dos veces, incluso ante intentos
concurrentes, mostrando al paciente disponibilidad en tiempo real y una alternativa cuando la franja
elegida ya no esté libre.

Enfoque técnico: servicio backend Spring Boot organizado en Clean Architecture (dominio / aplicación
/ infraestructura), exponiendo una API REST definida primero como contrato OpenAPI (API First) y
generada con `openapi-generator`. La prevención de doble reserva se garantiza con una restricción de
unicidad a nivel de base de datos sobre `(medicoId, fecha, horaInicio)` más bloqueo optimista como
defensa adicional. La notificación por WhatsApp se abstrae detrás de un puerto de salida
(`NotificationPort`) para no acoplar el registro de la cita al proveedor externo. Las pruebas cubren
tres niveles BDD (unitarias, integración, funcionales vía Cucumber) con cobertura verificada por
JaCoCo (>80% por clase, ≥80% global).

## Technical Context

**Language/Version**: Java 21 (toolchain ya configurado en `build.gradle`)

**Primary Dependencies**: Spring Boot 4.1.0 (`spring-boot-starter-webmvc`, `spring-boot-starter-data-jpa`), Lombok, Cucumber-JVM (`cucumber-java`, `cucumber-junit-platform-engine`) para pruebas funcionales BDD, `openapi-generator-gradle-plugin` para generación de interfaces de servidor desde el contrato, `jacoco` (plugin Gradle) para cobertura

**Storage**: H2 (motor de base de datos para desarrollo/test, ya declarado en `build.gradle`), acceso vía Spring Data JPA

**Testing**: JUnit 5 + Mockito (unitarias sobre casos de uso, mockeando puertos), `@DataJpaTest`/`@SpringBootTest` con H2 (integración de persistencia y restricciones de unicidad — H2 explícitamente permitido por la constitución v1.0.3 para pruebas de integración, sin necesidad de Testcontainers), Cucumber-JVM sobre JUnit 5 Platform + `MockMvc`/`@SpringBootTest` (funcionales, feature files derivados de los escenarios Gherkin de `spec.md`)

**Target Platform**: Servicio backend REST (API), desplegable en cualquier entorno compatible con JVM 21 (Linux server / contenedor); sin alcance de frontend/UI en esta historia

**Project Type**: Web service (backend API único — Opción 1: Single project)

**Performance Goals**: Sin metas de throughput extremo; suficiente para el volumen de una clínica (decenas de reservas concurrentes), priorizando corrección (cero dobles reservas) sobre rendimiento bruto

**Constraints**: El registro de la cita (FR-003) no debe depender del éxito del envío de WhatsApp (FR-004/FR-010); la validación de disponibilidad debe ser atómica ante concurrencia (FR-005/FR-009)

**Scale/Scope**: Alcance de una sola clínica/consultorio; 2 historias de usuario (reserva exitosa, prevención de doble reserva) — ver Assumptions en `spec.md` para límites explícitos (auth de paciente, gestión de agendas médicas y validación del número de WhatsApp quedan fuera de alcance)

## Constitution Check

*GATE: Must pass before Phase 0 research. Re-check after Phase 1 design.*

| Principio | Evaluación | Estado |
|---|---|---|
| I. Clean Architecture | Paquetes `domain` (entidades puras), `application` (casos de uso + puertos), `infrastructure` (JPA, web, notificación) con dependencias apuntando siempre hacia adentro; los adaptadores implementan puertos definidos en `application`. | PASS |
| II. BDD | Los dos escenarios Gherkin de `spec.md` se llevan 1:1 a features de Cucumber (funcionales); se añaden pruebas unitarias (casos de uso con mocks) e integración (persistencia + restricción de unicidad). | PASS |
| III. SOLID / YAGNI / DRY | Puertos (`CitaRepositoryPort`, `NotificationPort`) aplican Inversión de Dependencias y Segregación de Interfaces; no se introduce infraestructura de colas/mensajería ni bloqueo distribuido no requerido por las 2 historias actuales (YAGNI); un único punto de validación de disponibilidad reutilizado por ambos flujos (DRY). | PASS |
| IV. API First & OpenAPI | Contrato `contracts/openapi.yaml` escrito antes que el código; `openapi-generator` genera las interfaces de servidor a partir del contrato (dirección API-First, no code-first). | PASS |
| V. JaCoCo (cobertura) | Se añade el plugin `jacoco` con `jacocoTestCoverageVerification` (regla `CLASS` ≥0.80, regla `BUNDLE` ≥0.80) enlazado a `check`. | PASS |

Sin violaciones — no se requiere la tabla de Complexity Tracking.

## Project Structure

### Documentation (this feature)

```text
specs/001-reserva-cita-online/
├── plan.md              # This file (/speckit-plan command output)
├── research.md          # Phase 0 output (/speckit-plan command)
├── data-model.md         # Phase 1 output (/speckit-plan command)
├── quickstart.md         # Phase 1 output (/speckit-plan command)
├── contracts/
│   └── openapi.yaml      # Phase 1 output (/speckit-plan command)
└── tasks.md              # Phase 2 output (/speckit-tasks command - NOT created by /speckit-plan)
```

### Source Code (repository root)

```text
# Opción 1: Single project (Spring Boot backend, Clean Architecture por paquete)
src/main/java/com/mikels/citasalud/
├── domain/
│   ├── model/            # Paciente, Medico (proyecciones mínimas de solo lectura), Cita, FranjaHoraria (POJOs sin anotaciones JPA)
│   └── exception/         # FranjaNoDisponibleException, RecursoNoEncontradoException
├── application/
│   ├── port/
│   │   ├── in/             # ReservarCitaUseCase, ConsultarFranjasDisponiblesUseCase
│   │   └── out/            # CitaRepositoryPort, FranjaHorariaRepositoryPort, NotificationPort, PacienteRepositoryPort, MedicoRepositoryPort
│   └── service/            # ReservarCitaService, ConsultarFranjasDisponiblesService
└── infrastructure/
    ├── persistence/
    │   ├── entity/         # CitaJpaEntity, FranjaHorariaJpaEntity (anotaciones JPA + @Version), PacienteJpaEntity, MedicoJpaEntity (solo lectura)
    │   ├── repository/     # Spring Data JPA repositories (incluye Paciente/Medico, solo lectura)
    │   └── adapter/         # Implementaciones de todos los puertos de salida (application/port/out)
    ├── web/
    │   ├── controller/     # Implementan las interfaces generadas por openapi-generator
    │   └── mapper/          # DTO (generado) ↔ modelo de dominio
    ├── notification/       # WhatsAppNotificationAdapter implementando NotificationPort
    └── config/              # Configuración de beans, manejo global de excepciones (@ControllerAdvice)

src/main/resources/
├── application.yaml         # Datasource H2 + `spring.jpa.hibernate.ddl-auto` (esquema generado desde las entidades JPA) + `spring.sql.init` para cargar datos semilla después del esquema
└── db/                      # data.sql / esquema semilla para desarrollo (médicos, pacientes y franjas de ejemplo)

src/test/java/com/mikels/citasalud/
├── unit/                     # JUnit 5 + Mockito sobre application/service (mockeando puertos)
├── integration/              # @DataJpaTest / @SpringBootTest con H2 (persistencia, restricción única)
└── bdd/                       # Step definitions de Cucumber + runner JUnit 5 Platform

src/test/resources/features/  # Archivos .feature (Gherkin) derivados de spec.md
```

**Structure Decision**: Proyecto único (no hay frontend en el alcance de esta historia). Se adopta
Clean Architecture por paquete (`domain` → `application` → `infrastructure`) dentro del mismo módulo
Gradle existente (`citasalud-service-redo`), evitando la sobre-ingeniería de múltiples módulos Gradle
para solo 2 historias de usuario (YAGNI). Las pruebas se organizan en subpaquetes (`unit`,
`integration`, `bdd`) dentro de `src/test/java`, sin necesidad de source sets adicionales de Gradle.

## Complexity Tracking

> No aplica — el Constitution Check no presenta violaciones que requieran justificación.
