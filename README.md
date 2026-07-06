# citasalud-service-redo

Servicio backend de Citasalud: reserva de citas médicas en línea.

## Requisitos

- JDK 21
- No requiere instalación adicional de base de datos (H2 en memoria, autoconfigurada)

## Levantar la aplicación

```bash
./gradlew bootRun
```

La API queda disponible en `http://localhost:8080/api/v1`. La consola H2 está disponible en
`http://localhost:8080/api/v1/h2-console` (JDBC URL: `jdbc:h2:mem:citasalud`, usuario `sa`, sin
contraseña).

## Ejecutar pruebas

```bash
./gradlew test                            # unitarias + integración + funcionales (Cucumber)
./gradlew jacocoTestReport                # reporte de cobertura (build/reports/jacoco)
./gradlew jacocoTestCoverageVerification   # falla si no se cumple >80% por clase / >=80% global
```

## Feature: Reserva de Cita en Línea 24/7

Ver [`specs/001-reserva-cita-online/quickstart.md`](specs/001-reserva-cita-online/quickstart.md) para
la guía de validación end-to-end (escenarios de reserva exitosa, franja ocupada y concurrencia), y
[`specs/001-reserva-cita-online/`](specs/001-reserva-cita-online/) para la especificación completa,
el plan de implementación y el contrato OpenAPI.

## Arquitectura

El proyecto sigue Clean Architecture (Robert C. Martin) organizada por paquete bajo
`com.mikels.citasalud`:

- `domain`: modelos y excepciones de negocio, sin dependencias de frameworks.
- `application`: casos de uso y puertos (entrada/salida), orquestando el dominio.
- `infrastructure`: adaptadores (JPA, controladores REST, notificación WhatsApp) que implementan
  los puertos de `application`.

Ver la [constitución del proyecto](.specify/memory/constitution.md) para los principios y estándares
de desarrollo obligatorios (Clean Architecture, BDD, SOLID/YAGNI/DRY, API First, cobertura JaCoCo).
