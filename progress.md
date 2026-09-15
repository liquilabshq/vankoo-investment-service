# progress.md — Estado de la sesión actual

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
