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
