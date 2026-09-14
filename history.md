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
