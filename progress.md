# progress.md — Estado de la sesión actual

## Sesión 2026-11-05 — feature 11 azure-deployment

- Rama aprobada: `feature/azure-deployment-11`, desde develop actualizado.
- Feature 10 cerrada administrativamente: review APPROVED y código ya integrado en develop;
  se conserva abajo la evidencia histórica original. No se vuelve a ejecutar su despliegue local.
- Plan: perfil Azure independiente; control de mensajería preservando outbox; confianza TLS en
  Docker; Terraform aislado en Infra; pruebas y guía de evidencias Sprint 4.
- Infra usa la rama aprobada `feature/investment-azure-deployment` y un estado propio para no
  modificar recursos de otros servicios. El despliegue real posterior se registra más abajo.
- Implementación local lista, verificación en curso. No marcar done hasta review.
- `mvn test -q` con Maven 3.9.12/JDK 25: salida 0. La prueba Azure usa H2 aislado
  (Flyway desactivado solo en el test); confirma ausencia de consumidores y publisher,
  y persistencia transaccional de subasta + outbox PENDING. No prueba Oracle ni TCPS.
- Infra: `terraform validate` OK y cuatro `terraform test` con mocks OK.
- Plan remoto bootstrap consultado sin apply: 16 altas, 0 modificaciones, 0 eliminaciones.
  Reutiliza vankoo-env/ACR existentes; no administra los otros servicios.
- Clave SSH pública nueva disponible fuera de Git. La privada no se lee ni se imprime.
- `mvn clean test -q`: salida 0. Reportes XML limpios: 117 casos reportados, 9 omitidos,
  0 fallos y 0 errores (los motores JUnit/Cucumber pueden contabilizar contenedores).
- Build Docker completo: correcto tras reintentar el fallo DNS de Maven Central.
  Imagen local `vankoo-investment-service:azure-validation`; no publicada.
- Prueba de entrypoint sobre Temurin 25 JRE con certificado sintético: `ENTRYPOINT_TLS_TRUST_OK`.
  Conserva autoridades públicas para no bloquear futura conexión TLS con Event Hubs.
- Al terminar la preparación inicial todavía no había VM ni evidencia de despliegue real.
- Preflight remoto posterior: plan bootstrap sigue en 16 altas/0 cambios/0 bajas.
  Microsoft.Compute y Microsoft.Network están NotRegistered en la suscripción;
  Microsoft.App está Registered. No se ejecutó apply: falta registrar los dos
  proveedores con permisos de suscripción antes de crear la VM y su red.

## Actualización — despliegue real y publicación de ramas

- La persona usuaria ejecutó Terraform desde la rama Infra. B2s y B2als_v2 no
  tuvieron capacidad; D2s_v4 permitió crear la VM Oracle. Posteriormente se registró
  DevTestLab y se completó el apagado automático a las 23:00 de Lima.
- Oracle healthy y stunnel active confirmados mediante Azure Run Command.
- Imagen publicada: `investment-service:5a7f8b9-dirty-20261106012449`. La imagen
  incluye los cambios locales; no se presenta como artefacto de un commit limpio.
- Container App con imagen real, Succeeded/Running y revisión lista 0000001;
  readiness devolvió UP e incluye la base de datos.
- POST sintético: 201 y subasta `19e7cd72-528b-44d9-a14f-2b5651aeb859`.
  GET: 200 con PEN 1000.00. Después de reiniciar solo Investment, una réplica nueva
  respondió 200 con el mismo registro. No se probó reiniciar Oracle ni la VM.
- Evidencias en tsp-report, sección 5.2.4.7. La captura del GET posterior al reinicio
  está pendiente; la salida se compartió como texto. No se inspeccionó un listado de
  migraciones Flyway en Azure.
- Gateway, Risk y Event Hubs siguen fuera del alcance. JWT propio de esta capa:
  coordinar alineación antes de integrar. La mensajería permanece desactivada.
- Se solicitó expresamente crear commits y publicar solo las ramas Investment e
  Infra, sin PR ni cambios de otros repositorios. Feature 11 sigue in_progress
  hasta completar revisión/verificación; no se reutiliza el APPROVED de feature 10.

## Registro conservado de la feature 10

> Se reinicia al cerrar cada sesión: el resumen se agrega a `history.md`.
> Si contiene una feature, notas o un review al iniciar, léelo antes de actuar.

## Feature en curso

- id 10 — dockerize-investment-service — Dockerizar Investment Service e integrarlo en la
  infraestructura compartida.

## Rama

- feature/dockerize-investment-service-10 (creada desde feature/expose-auction-participant-query-apis-8
  tras el push de sus 3 commits de cierre; esa rama es idéntica a `origin/develop` + el cierre de
  la feature 8, ya que el PR #11 con el código de la feature 8 ya estaba mergeado a develop antes
  de crear esta rama).

## Plan

1. Crear `Dockerfile` multi-stage en la raíz de este repo (build con Maven 3.9.12/Temurin 25,
   runtime con `eclipse-temurin:25-jre-jammy`), siguiendo el mismo patrón ya usado en
   `vankoo-iam-service/Dockerfile`.
2. Crear `src/main/resources/application-docker.yaml` con datasource Oracle, binder Kafka y
   Eureka resueltos por variables de entorno (`INVESTMENT_DB_HOST`, `KAFKA_HOST`/
   `KAFKA_INTERNAL_PORT`, `DISCOVERY_SERVER_URL`), análogo al de `vankoo-iam-service`.
3. Agregar el bloque `investment-service` en `vankoo/infrastructure/docker-compose.yaml` bajo el
   placeholder ya existente, con `depends_on` sobre `investment-db-oracle` (sin healthcheck propio
   → `service_started`), `kafka-broker` y `discovery-server` (`service_healthy`), puerto host
   `8083` y healthcheck sobre `/actuator/health/readiness`.
4. Agregar `INVESTMENT_SERVICE_PORT`/`INVESTMENT_SERVICE_HOST` a `vankoo/infrastructure/.env.example`.
5. Validar sintaxis (`docker compose config`), build de imagen (`docker build`) y que `mvn test`
   siga en verde (no se tocó código Java, solo config/Docker).

## Notas de la sesión

- Se cerró y archivó la feature 8 en esta misma cadena de ramas antes de empezar (ver history.md).
- El compose de `vankoo/infrastructure` ya traía `investment-db-oracle` listo (BD) desde antes;
  esta feature solo agrega el contenedor de la aplicación.
- Cambios en 2 repos: `vankoo-investment-service` (Dockerfile, application-docker.yaml,
  feature_list.json, progress.md) e `infrastructure` (docker-compose.yaml, .env.example) — la
  integración con infraestructura se gestiona en su propia rama (`feature/investment-service`),
  fuera del harness de este repo.

## Evidencia de aceptación

1. "Existe un Dockerfile multi-stage... sin credenciales embebidas." -> `Dockerfile` en la raíz,
   build Maven + runtime `eclipse-temurin:25-jre-jammy`, sin valores de credenciales hardcodeados
   (usuario `spring` no root). Build real confirmado (ver Verificación).
2. "Existe `application-docker.yaml`... sin valores por defecto que expongan secretos." ->
   `src/main/resources/application-docker.yaml`, hosts/puertos resueltos por variables de entorno;
   el único default embebido es el nombre de host de servicio (`investment-db-oracle`,
   `kafka-broker`), no una credencial.
3. "`docker compose up -d --build investment-service`... sin errores, conectado a
   investment-db-oracle, kafka-broker y discovery-server." -> Ejecutado desde
   `vankoo/infrastructure` (rama `feature/investment-service`); los 4 contenedores
   (`investment-service`, `investment-db-oracle`, `kafka-broker`, `discovery-server`) llegaron a
   `Up`/`healthy` sin reinicios ni errores. Logs confirman migraciones Flyway (3) aplicadas contra
   Oracle, conexión Hibernate a `jdbc:oracle:thin:@investment-db-oracle:1521/FREEPDB1`, y
   suscripción a los canales Kafka `processInvoiceEligibleForFunding-in-0` y
   `projectAuctionMarketplace-in-0`.
4. "`GET /actuator/health` responde 200 con el servicio registrado en Eureka." ->
   `curl http://localhost:8083/actuator/health` -> `{"status":"UP", "groups":["liveness","readiness"]}`.
   `curl http://localhost:8761/eureka/apps` -> aplicación `INVESTMENT-SERVICE`, instancia
   `investment-service:investment-service:8082`, `status":"UP"`.
5. "`docker-compose.yaml`/`compose.yaml` no se agregan a este repo." -> confirmado; ambos archivos
   nuevos viven solo en `vankoo/infrastructure`. `git status --short` en
   `vankoo-investment-service` no muestra ningún archivo de compose.

## Verificación

- `mvn test` (Maven 3.9.12 vía wrapper cache, JDK 25) -> BUILD SUCCESS. 60/60 tests, 0 failures,
  0 errors, 0 skipped (`target/surefire-reports`).
- `docker compose config -q` (desde `vankoo/infrastructure`) -> OK, sin errores de sintaxis.
- `docker build -t vankoo-investment-service:test .` -> éxito, imagen de 681MB.
- `docker compose up -d --build investment-service` (desde `vankoo/infrastructure`) -> los 4
  contenedores quedaron `Up`/`healthy`: `investment-service` (healthcheck
  `/actuator/health/readiness`), `investment-db-oracle`, `kafka-broker`, `discovery-server`.
- `GET http://localhost:8083/actuator/health` -> 200, `{"status":"UP"}`.
- `GET http://localhost:8761/eureka/apps` -> `INVESTMENT-SERVICE` registrado, `status: UP`.
- Verificación manual: se corrió `docker compose down` sin acotar a servicios, lo que además
  detuvo y eliminó `iam-db-postgres` e `invoicing-db-mongo` (contenedores preexistentes,
  detenidos, de trabajo previo ajeno a esta feature). Los volúmenes de datos
  (`vankoo_iam-db-data`, `vankoo_invoicing-db-data`) quedaron intactos; a pedido explícito de la
  persona usuaria no se recrearon los contenedores en esta sesión.

## Bloqueos

-

## Review — feature 10
**Veredicto:** APPROVED

### Checkpoints
- C1: [x] Evidencia: los 5 criterios de `acceptance` están enumerados con evidencia real en la
  sección "Evidencia de aceptación" (build, `docker compose up`, health, Eureka, ausencia de
  compose en este repo).
- C2: [x] No aplica — esta feature no toca `src/main/java/.../domain`. Sin cambios de dominio.
- C3: [x] No aplica — no se tocaron query services.
- C4: [x] No aplica — no se agregaron ni modificaron controllers/consumers.
- C5: [x] No aplica — no se agregaron ni modificaron eventos de dominio ni handlers.
- C6: [x] Evidencia: `rg -n 'System\.out\.|TODO' src` no devuelve resultados.
- C7: [x] Evidencia: `mvn test` -> BUILD SUCCESS, 60/60, 0 failures/errors/skipped
  (`target/surefire-reports`).
- C8: [x] Evidencia: `git ls-files .env .env.* '*.pfx' '*.pem' '*.key'` no devuelve resultados;
  `Dockerfile` y `application-docker.yaml` solo referencian variables de entorno, sin valores de
  credenciales reales.
- C9: [x] Evidencia: exactamente 1 feature `in_progress` (id 10).

### Cambios requeridos
Ninguno.

## Cierre

- Feature 10 verificada end-to-end y con review APPROVED. Lista para el plan de commits en los 2
  repos (`vankoo-investment-service` e `infrastructure`).

## Review — feature 11
**Veredicto:** APPROVED

Revisión independiente del código de la feature 11, sin modificar implementación ni backlog.
La autorización de commits/push no se extiende a Gateway, Event Hubs ni otros servicios.

### Evidencia de aceptación

1. Selección del perfil Azure: `Dockerfile:32-33` usa `SPRING_PROFILES_ACTIVE=docker`
   como valor sustituible y delega en `docker/entrypoint.sh`, sin forzar un perfil con
   `-Dspring.profiles.active`. El despliegue real registrado arriba arrancó con `azure`.
2. Configuración Azure segura: `src/main/resources/application-azure.yaml` exige las
   variables de conexión/credenciales, usa Hibernate `validate`, Flyway, exposición
   Actuator `health,info` y readiness `readinessState,db`. El entrypoint añade solo el
   certificado público de Oracle y conserva las autoridades del JRE.
3. Sin Kafka y con outbox: `AzureStandaloneIntegrationTest:43` verifica ausencia de
   bindings/consumers/publisher y persistencia de Auction + outbox PENDING en H2 aislada.
   `StandaloneMessagingConfigurationTest:25,33` verifica desactivación del publisher
   y compatibilidad local cuando no se especifica la propiedad.
4. Verificación mínima satisfecha: nueva ejecución `mvn test -q`, Maven 3.9.12/JDK 25,
   salida 0; reportes Surefire con 117 casos reportados, 9 omitidos, 0 fallos y 0 errores
   (incluyen contenedores de motores JUnit/Cucumber). Oracle/TCPS y Azure se documentan
   por separado en la actualización de despliegue y en la guía de validación de Infra.

### Checkpoints

- C1: [x] Los cuatro criterios de feature 11 están enumerados arriba con evidencia.
- C2: [x] Sin coincidencias del patrón de dependencias prohibidas en `domain`; el diff
  no modifica dominio ni introduce dependencias hacia otras capas.
- C3: [x] Sin escrituras `save/saveAll/delete/deleteAll/flush` en query services; sin
  cambios en queries para esta feature.
- C4: [x] `InvoicingOcrEventConsumer.processInvoiceEligibleForFunding` conserva la
  conversión por assembler y delegación en `AuctionCommandService`; el consumer
  `AuctionMarketplaceEventConsumer.projectAuctionMarketplace` conserva assembler y
  `AuctionMarketplaceProjectionService`. Solo se añadió la condición de configuración,
  sin repositorios ni reglas de negocio nuevas en adaptadores.
- C5: [x] No se modifican transiciones, agregados ni handlers de proyección. El listener
  existente `InvestmentOutboxEventListener` conserva `BEFORE_COMMIT` y la persistencia
  del outbox; el test Azure comprueba que el hecho se conserva con mensajería apagada.
- C6: [x] `git diff --check` correcto; sin `System.out` ni `TODO` en `src`. Cambios de
  configuración acotados a la feature, sin lógica de negocio duplicada.
- C7: [x] `mvn test -q` fresco termina en 0; reportes XML inspeccionados. El test de
  publicación fallida registra una excepción esperada sin fallar la suite. La prueba
  del entrypoint en Temurin 25, sin red y con certificado sintético, devolvió
  `ENTRYPOINT_TLS_TRUST_OK` y salida 0, según la ejecución comunicada por main.
- C8: [x] `git ls-files .env .env.* '*.pfx' '*.pem' '*.key'` sin resultados. Archivos
  nuevos inspeccionados: sin material privado ni credenciales reales. Los valores de
  prueba están identificados como fixtures; `changeit` protege solo certificados
  públicos en un truststore temporal, no una clave privada de producción.
- C9: [x] Exactamente una feature `in_progress` (id 11); feature 10 tiene su review
  histórico propio. No se cambia feature 11 a `done` en esta revisión.

### Cambios requeridos

Ninguno para el alcance independiente aprobado. La integración con Gateway/Risk/Event
Hubs y la recuperación tras reiniciar Oracle/VM siguen fuera de esta aceptación.

### Revisión complementaria — finales de línea

Se inspeccionó `.gitattributes`: `docker/*.sh text eol=lf` limita la normalización a
los scripts Linux de esta feature. `git check-attr text eol` confirma `text: set` y
`eol: lf` para `docker/entrypoint.sh` y `docker/test-entrypoint.sh`. El cambio evita
CRLF por `core.autocrlf` en Windows sin cambiar lógica ni ampliar el alcance. Conforme;
se mantiene el veredicto APPROVED.

## Commits y publicación autorizados

Se crearon commits por archivo seguro, sin estados, planes, claves, archivos de
entorno ni credenciales. La persona usuaria autorizó expresamente commit y push
solo de Investment e Infra; no se abrieron PR ni se hicieron applies/redespliegues.

- `b91e833`: `.dockerignore`.
- `19e2078`: `docker/entrypoint.sh`.
- `ffacf7b`: `docker/test-entrypoint.sh`.
- `0f6235d`: `Dockerfile`.
- `91d3d37`: `src/main/resources/application-azure.yaml`.
- `5c62838`: `OutboxPublisher.java`.
- `75b184b`: `AuctionMarketplaceEventConsumer.java`.
- `cb695a8`: `InvoicingOcrEventConsumer.java`.
- `577b4b3`: `StandaloneMessagingConfigurationTest.java`.
- `2dd07e1`: `AzureStandaloneIntegrationTest.java`.
- `962a3f1`: `feature_list.json` (seguimiento, sin cerrar feature 11).
- `62371ab`: `.gitattributes`, LF para scripts Linux.

Primera publicación confirmada: `git push -u origin feature/azure-deployment-11`,
salida 0, rama remota creada y upstream configurado en `62371ab`.
Esta bitácora se incluye en un commit posterior y se publica en la misma rama.

Infra publicó `feature/investment-azure-deployment` con salida 0 y upstream
configurado en `90ad1af`. Commits: `72eea4c` (ignorados), `8e50c9b` (capa Terraform),
`c4c78c8` (operación), `f7273c6` (guías), `519a1d7` (checksums Windows, sin cambios de
versiones de providers) y `90ad1af` (LF). El tamaño por defecto coincide con D2s_v4;
preflight incluye DevTestLab. Integración de los PR con develop pendiente.

La feature conserva `in_progress` para su cierre formal y archivo de sesión; no se
presenta la publicación de la rama como merge ni como integración con otros servicios.
