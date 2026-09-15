# history.md — Bitácora de sesiones cerradas

> No edites entradas previas. Al cerrar una sesión, agrega una entrada al final.

Formato de entrada:

```markdown
## YYYY-MM-DD — Feature <id> <name> — done|blocked
- Resumen: ...
- Verificación: <comando> -> OK|FAIL
- Archivos tocados: ...
- Veredicto del reviewer: APPROVED|CHANGES_REQUESTED|No solicitado
- Commits: <hashes> | decisión explícita de no crear commits
```

---

## 2026-09-14 — Feature 2 define-marketplace-read-model-boundary — done
- Resumen: se asignó a `investment-service` la propiedad del read model Marketplace y se
  documentaron consulta, visibilidad, topic único, contratos versionados, frescura y replay. Como
  corrección de checkpoint, el cálculo financiero pasó a depender del contrato de dominio
  `AuctionPricingPolicy` en lugar de infraestructura.
- Verificación: `mvn -o "-Dmaven.repo.local=C:\Users\user\.m2\repository" test` -> OK (17 pruebas).
- Archivos tocados: `docs/ARCHITECTURE.md`, `docs/MARKETPLACE_READ_MODEL.md`,
  `AuctionPricingPolicy.java`, `AuctionPricingCalculator.java`, `PricingProperties.java`,
  `feature_list.json`, `progress.md`.
- Veredicto del reviewer: APPROVED.
- Commits: `fc159eb`, `87598d6`, `b274198`.

## 2026-09-14 — Feature 4 define-versioned-auction-integration-event-contract — done
- Resumen: se congeló el envelope v1 y los payloads tipados para siete eventos del ciclo de vida,
  con topic único, JSON Schema, ejemplos, política de compatibilidad y regresión del contrato
  inbound de Invoicing.
- Verificación: `mvn -o "-Dmaven.repo.local=C:\Users\user\.m2\repository" test` -> OK (20 pruebas).
- Archivos tocados: `interfaces/events/resources`, `docs/contracts/auction-lifecycle/v1`, pruebas
  de contrato, `feature_list.json`, `progress.md`.
- Veredicto del reviewer: APPROVED.
- Commits: `97d0e74`, `b42d16a`, `a00f04f`.

## 2026-09-14 — Feature 5 reliably-publish-auction-lifecycle-events — done
- Resumen: se completaron los eventos de dominio del ciclo de vida, su mapeo explícito al
  contrato v1 y un outbox transaccional Oracle con publicación Kafka at-least-once, key por
  `auctionId`, confirmación síncrona, reintento con `eventId` estable, backoff y limpieza.
- Verificación: `mvn -o "-Dmaven.repo.local=C:\Users\user\.m2\repository" test` -> OK
  (29 pruebas).
- Archivos tocados: eventos de dominio de Auction, `infrastructure/messaging/outbox`, migración
  `V2__create_investment_outbox.sql`, configuración Kafka, documentación y pruebas del outbox.
- Veredicto del reviewer: APPROVED.
- Commits: `211d4aa`, `980e2f6`, `1155980`, `44268d4`, `a6c2b86`.

## 2026-09-14 — Feature 6 build-kafka-marketplace-read-model-projection — done
- Resumen: se añadió la proyección Kafka transaccional del ciclo de vida sobre una vista Oracle
  separada y un inbox durable. Deduplica por `eventId`, protege el watermark de secuencia,
  difiere eventos sin publicación previa, drena prerequisitos en orden y conserva estados
  terminales; Marketplace dejó de consultar el agregado para su listado.
- Verificación: `mvn -o "-Dmaven.repo.local=C:\Users\user\.m2\repository" test` -> OK
  (38 pruebas).
- Archivos tocados: comando/servicio de proyección, consumer/assembler/parser Kafka,
  `application/internal/eventhandlers`, entidades/repositorios de vista e inbox, migración V3,
  configuración, query service, documentación y pruebas.
- Veredicto del reviewer: APPROVED.
- Commits: `48e2b95`, `241e5a9`, `6e190b8`, `5451379`, `3af42dc`, `39ea82b`.

## 2026-09-14 — Feature 1 add-auction-domain-test-baseline — done
- Resumen: se agregó cobertura de test para el estado inicial de Auction
  (PENDING_VERIFICATION_RISK, financiamiento en cero, riskScore pendiente), el rechazo general de
  inversiones por debajo del ticket mínimo en un escenario de remanente amplio, y la emisión de
  PartitionAddedEvent/AuctionFullyFundedEvent al completar el financiamiento, verificada con
  ApplicationEvents/@RecordApplicationEvents de Spring en un test de integración. A pedido
  explícito del usuario se incluyó además, en la misma rama, un cambio de configuración (default
  de minimum-investment de 500.00/150.00 a 50.00/15.00 PEN/USD) y una aclaración en la skill
  create-git-commit (prohibir trailer de coautoría de IA).
- Verificación: mvn test -> OK (20/20, BUILD SUCCESS; corrida por el implementer y reejecutada de
  forma independiente por el reviewer).
- Archivos tocados: AuctionLifecycleTest.java, AuctionFinancialFlowIntegrationTest.java,
  feature_list.json, progress.md, application.yaml, .agents/skills/create-git-commit/SKILL.md.
- Veredicto del reviewer: APPROVED
- Commits: 57c91a7, ece1679, c8cad1d, 37ff30d, 1b706bf, 2205633

## 2026-09-14 — Feature 9 fix-pricing-calculator-domain-layering-violation — done
- Resumen: AuctionPricingCalculator (domain/services) importaba y recibía en su constructor
  com.liquilabs.vankoo.investment.infrastructure.configuration.PricingProperties, violando C2
  (el dominio no debe depender de application/infrastructure/interfaces). Se creó el value
  object de dominio PricingParameters (domain/model/valueobjects) con únicamente los datos que
  el calculator necesita (version, dayCountBasis, platformMonthlyFeeRate, platformFeeTaxRate,
  investorTea/teaFor); AuctionPricingCalculator quedó stateless y su método calculate recibe
  PricingParameters como primer parámetro. AuctionCommandServiceImpl.handle(CreateFinancialQuoteCommand)
  traduce PricingProperties a PricingParameters justo antes de invocar el calculator, sin
  duplicar reglas de negocio. PricingProperties no cambió: zoneId, quoteValidity, fundingWindow,
  settlementBuffer y minimumInvestment siguen siendo configuración técnica consumida
  directamente por application.
- Verificación: mvn test -> OK (20/20, BUILD SUCCESS; corrida por el implementer y reejecutada
  de forma independiente por el reviewer).
- Archivos tocados: PricingParameters.java (nuevo), AuctionPricingCalculator.java,
  AuctionCommandServiceImpl.java, AuctionPricingCalculatorTest.java, AuctionLifecycleTest.java,
  AuctionRepositoryTest.java, feature_list.json, progress.md.
- Veredicto del reviewer: APPROVED
- Commits: 47fff80, 97cca69, 49d995c, d8b3dca, edde20d, 38969c3, a660f04, 3c686d1

## 2026-09-14 — Feature 3 add-auction-expiration-lifecycle-event — done
- Resumen: la funcionalidad de expiración ya estaba implementada en el dominio
  (AuctionExpirationScheduler -> AuctionCommandServiceImpl.expireDueAuctions() ->
  Auction.expireIfDue(), que valida la transición y registra AuctionExpiredEvent una única vez
  gracias al guard de status). Se agregaron los 2 casos de test que faltaban en
  AuctionLifecycleTest: doesNotExpireAuctionsThatWereNeverPublished (estado DRAFT que no puede
  expirar) y doesNotExpireOrReEmitOnceAnAuctionAlreadyExpired (ausencia de doble emisión). A
  pedido explícito del usuario se alineó también src/test/resources/application.yaml
  (minimum-investment 500.00/150.00 -> 50.00/15.00) con el default ya cambiado en
  src/main/resources/application.yaml.
- Verificación: mvn test -> OK (22/22, BUILD SUCCESS; corrida por el implementer y reejecutada
  de forma independiente por el reviewer, dos veces, tras detectar y corregir un desvío de rama
  compartida con otra sesión).
- Archivos tocados: AuctionLifecycleTest.java, src/test/resources/application.yaml,
  feature_list.json, progress.md.
- Veredicto del reviewer: APPROVED
- Commits: 12255d7, dc6e5a3, c185fa3, c31c7e1

## 2026-09-14 — Feature 7 expose-paginated-marketplace-search-api — done
- Resumen: se expuso Marketplace como consulta paginada sobre la proyección, con filtros JPA
  combinables, estados terminales consultables, whitelist de orden, desempate estable y errores
  HTTP 400. Se añadió caché Caffeine por query con TTL de 30 segundos e invalidación después del
  commit de cada actualización proyectada.
- Verificación: `mvn -o "-Dmaven.repo.local=C:\Users\user\.m2\repository" test` -> OK
  (55 pruebas) después de integrar `origin/develop`.
- Archivos tocados: modelos/query service de Marketplace, Specification/repositorio JPA,
  controller/assembler/resource REST, configuración e invalidación Caffeine, documentación y
  pruebas de integración.
- Veredicto del reviewer: APPROVED.
- Commits: `4b48c1a`, `7048c62`, `0dc8f2b`, `679499b`, `47f0358`, `b49bd38`.
- Integración previa de `origin/develop`: `1e7773d`.

## 2026-09-15 — Feature 8 expose-auction-detail-and-participant-query-apis — done
- Resumen: se expusieron GET /auctions/mype/{mypeId} y GET /auctions/investor/{investorId} como
  queries CQRS (GetAuctionsByMypeQuery, GetAuctionsByInvestorQuery) delegadas a
  AuctionQueryServiceImpl; GET /auctions/{id} ya existía y se dejó sin el control de caller por
  ser una vista general de detalle. Se agregó autorización vía UnauthorizedAccessException (403)
  comparando el header X-User-Id contra el path variable en AuctionsController. AuctionRepository
  ganó findByMypeId y findByInvestorParticipation con @EntityGraph(quotes, partitions) para evitar
  LazyInitializationException fuera de la transacción (open-in-view: false). Se confirmó con test
  explícito que GetAllActiveAuctionsQuery ya filtraba por estados activos (findByStatusIn) desde
  antes de esta feature, sin exponer un endpoint nuevo para ello.
- Verificación: mvn test (Maven 3.9.10, JDK 25) -> OK (60/60, BUILD SUCCESS; corrida por el
  implementer y reejecutada de forma independiente por el reviewer).
- Archivos tocados: AuctionQueryServiceImpl.java, AuctionQueryService.java,
  UnauthorizedAccessException.java, GetAuctionsByInvestorQuery.java, GetAuctionsByMypeQuery.java,
  AuctionRepository.java, GlobalExceptionHandler.java, AuctionsController.java,
  AuctionRepositoryTest.java, AuctionFinancialFlowIntegrationTest.java, feature_list.json,
  progress.md.
- Veredicto del reviewer: APPROVED.
- Commits: `fbae08f`, `be66536`, `a0934db`, `12bb71e`, `c027842`.
