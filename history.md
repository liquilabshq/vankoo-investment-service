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
