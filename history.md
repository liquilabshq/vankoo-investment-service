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
