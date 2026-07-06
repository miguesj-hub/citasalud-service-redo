# Quickstart: Reserva de Cita en Línea 24/7

Guía de validación end-to-end para confirmar que la funcionalidad cumple los escenarios de
`spec.md`. No incluye código de implementación; ver `data-model.md` y `contracts/openapi.yaml`
para los detalles de modelo y API, y `tasks.md` (generado por `/speckit-tasks`) para el desglose de
implementación.

## Prerrequisitos

- JDK 21
- El proyecto se ejecuta con el wrapper de Gradle incluido (`./gradlew`)
- Base de datos H2 (en memoria, autoconfigurada — sin instalación adicional)
- Datos semilla: un `Medico` y al menos dos `FranjaHoraria` en estado `DISPONIBLE` para ese médico
  (cargados vía `data.sql` de perfil de desarrollo/test, o insertados manualmente vía consola H2)

## Levantar la aplicación

```bash
./gradlew bootRun
```

La API queda disponible en `http://localhost:8080/api/v1`.

## Escenario 1 — Reserva exitosa fuera de horario telefónico (US1)

Corresponde a: *Given* el paciente accede fuera del horario de atención telefónica, *When* elige
médico, fecha y hora disponibles y confirma, *Then* la cita queda registrada y recibe confirmación
por WhatsApp.

1. Consultar franjas disponibles del médico:

   ```bash
   curl -s "http://localhost:8080/api/v1/medicos/{medicoId}/franjas-disponibles?fecha=2026-07-10"
   ```

   **Resultado esperado**: HTTP 200 con al menos una franja en estado `DISPONIBLE`.

2. Confirmar la reserva sobre una de las franjas devueltas:

   ```bash
   curl -s -X POST http://localhost:8080/api/v1/citas \
     -H "Content-Type: application/json" \
     -d '{"pacienteId":"<pacienteId>","medicoId":"<medicoId>","franjaHorariaId":"<franjaId>"}'
   ```

   **Resultado esperado**: HTTP 201, cuerpo con `estado: "CONFIRMADA"`. Verificar en logs/adaptador
   de notificación que se generó el intento de envío por WhatsApp (`notificacionEnviada: true` en
   condiciones normales, o `false` con medio alterno visible si el proveedor no está disponible).

3. Repetir el paso 1 para la misma fecha/médico: la franja usada en el paso 2 ya NO debe aparecer
   como `DISPONIBLE`.

## Escenario 2 — Franja ya ocupada (US2)

Corresponde a: *Given* el paciente intenta seleccionar una franja ya ocupada, *When* intenta
confirmarla, *Then* el sistema la muestra como no disponible y lo invita a elegir otra.

1. Reutilizar la misma `franjaHorariaId` confirmada en el Escenario 1, paso 2.
2. Intentar reservarla nuevamente con un paciente distinto:

   ```bash
   curl -s -X POST http://localhost:8080/api/v1/citas \
     -H "Content-Type: application/json" \
     -d '{"pacienteId":"<otroPacienteId>","medicoId":"<medicoId>","franjaHorariaId":"<franjaId>"}'
   ```

   **Resultado esperado**: HTTP 409 con `codigo: "FRANJA_NO_DISPONIBLE"`, sin que se cree una
   segunda cita para esa franja.

## Escenario 3 — Confirmaciones simultáneas sobre la misma franja (concurrencia, US2)

Disparar dos solicitudes `POST /citas` en paralelo sobre la misma `franjaHorariaId` (por ejemplo,
con dos procesos `curl` lanzados casi al mismo tiempo, o un script de carga con 2 hilos).

**Resultado esperado**: Exactamente una solicitud recibe HTTP 201; la otra recibe HTTP 409. En la
base de datos solo debe existir una `Cita` `CONFIRMADA` para esa franja.

## Escenario 4 — Consultar estado tras perder la conexión (FR-011)

Corresponde al Edge Case de pérdida de conexión: el paciente confirmó una franja pero nunca recibió
la respuesta (o quiere verificar antes de reintentar). Consulta por `pacienteId` + `franjaHorariaId`
(los únicos datos que ya conocía antes de confirmar — no necesita el `id` de la cita).

1. Reutilizar el `pacienteId` y `franjaHorariaId` confirmados en el Escenario 1, paso 2:

   ```bash
   curl -s "http://localhost:8080/api/v1/citas?pacienteId=<pacienteId>&franjaHorariaId=<franjaId>"
   ```

   **Resultado esperado**: HTTP 200 con la misma cita (`estado: "CONFIRMADA"`). Repetir la misma
   consulta varias veces debe dar siempre el mismo resultado, sin crear una segunda cita
   (operación idempotente, de solo lectura).

2. Consultar con una combinación `pacienteId`/`franjaHorariaId` que nunca se reservó:

   **Resultado esperado**: HTTP 404 con `codigo: "RECURSO_NO_ENCONTRADO"`.

## Ejecutar la suite de pruebas

```bash
./gradlew test                       # unitarias + integración + funcionales (Cucumber)
./gradlew jacocoTestReport            # reporte de cobertura
./gradlew jacocoTestCoverageVerification  # falla si no se cumple >80% por clase / >=80% global
```

**Resultado esperado**: todas las pruebas pasan, incluyendo los escenarios Gherkin de `spec.md`
ejecutados vía Cucumber, y el build no falla por umbral de cobertura.
