# progress.md — Estado de la sesión actual

> Se reinicia al cerrar cada sesión: el resumen se agrega a `history.md`.
> Si contiene una feature, notas o un review al iniciar, léelo antes de actuar.

## Feature en curso

- id 1 — add-auction-domain-test-baseline — Agregar cobertura automatizada del agregado Auction.

## Rama

- feature/add-auction-domain-test-baseline (creada desde develop, aprobada explícitamente por
  el usuario con este nombre en lugar del formato `-<id>` propuesto por la skill
  create-git-branch).

## Plan

1. Agregar en AuctionLifecycleTest un test de creación que verifique status ==
   PENDING_VERIFICATION_RISK, currentFunding en cero y riskScore pendiente de evaluación.
2. Agregar un test directo de rechazo por monto menor al mínimo (500.00 PEN) en un escenario de
   remanente amplio, distinto del caso límite de remanente exacto ya cubierto.
3. Agregar cobertura de emisión de eventos usando ApplicationEvents/@RecordApplicationEvents de
   Spring en AuctionFinancialFlowIntegrationTest: verificar PartitionAddedEvent en una inversión
   parcial y AuctionFullyFundedEvent al completar el monto objetivo.
4. Ejecutar mvn test. Este entorno no tiene Maven ni mvnw en el PATH; si persiste, la feature
   queda blocked por C7 y se documenta aquí.
5. Repasar checkpoints C1, C6 y C9 antes de solicitar review.

## Notas de la sesión

- `mvn`/`mvnw` no estaban en el PATH del repo; se usó una distribución de Maven 3.9.10 ya
  cacheada en `C:\Users\paulf\.m2\wrapper\dists\apache-maven-3.9.10-bin\...` (de otro proyecto
  con wrapper) junto con JAVA_HOME=`C:\Users\paulf\.jdks\openjdk-25` para ejecutar la
  verificación. No se modificó nada del repositorio para lograrlo.

## Evidencia de aceptación

1. "Existen pruebas bajo src/test que cubren la creación de Auction y su estado inicial
   PENDING_VERIFICATION_RISK." -> Cubierto por
   AuctionLifecycleTest#startsInPendingVerificationRiskWithNoFundingAndNoRiskGrade.
2. "Las pruebas cubren que addInvestment rechaza un monto menor a 500.00 y que una inversión
   válida actualiza el financiamiento." -> Rechazo general cubierto por
   AuctionLifecycleTest#rejectsAnInvestmentBelowTheMinimumTicketWhenThereIsAmpleFundingRoomLeft
   (además del caso límite ya existente); la actualización de financiamiento con una inversión
   válida ya estaba cubierta por acceptsAQuoteAndAllocatesTheReceivableExactlyAcrossPartitions.
3. "Las pruebas cubren la emisión de PartitionAddedEvent y AuctionFullyFundedEvent al completar
   el monto objetivo." -> Cubierto por
   AuctionFinancialFlowIntegrationTest#publishesPartitionAddedAndAuctionFullyFundedEventsWhenFundingCompletes
   usando @RecordApplicationEvents/ApplicationEvents de Spring.
4. "mvn test termina correctamente." -> Ver Verificación.

## Verificación

- Comando: mvn test (Maven 3.9.10 cacheado localmente, JDK 25)
- Resultado: BUILD SUCCESS — Tests run: 20, Failures: 0, Errors: 0, Skipped: 0.
- Verificación manual: —

## Bloqueos

- Ninguno.

## Cierre

- Pendiente: feature activa, reviewer y decisión de commits.

## Review — feature 1

**Veredicto:** APPROVED

### Checkpoints

- C1: [x] Evidencia: `progress.md` (sección "Evidencia de aceptación") enumera los 4 criterios de
  `acceptance` de la feature 1 con el test concreto que los cubre. Verificado contra el código
  real:
  - Criterio 1 -> `AuctionLifecycleTest#startsInPendingVerificationRiskWithNoFundingAndNoRiskGrade`
    (líneas 24-33) comprueba `status == PENDING_VERIFICATION_RISK`, `currentFunding` en 0.00 PEN,
    `riskScore.grade() == UNDER_EVALUATION` y `partitions` vacío.
  - Criterio 2 -> `AuctionLifecycleTest#rejectsAnInvestmentBelowTheMinimumTicketWhenThereIsAmpleFundingRoomLeft`
    (líneas 36-46) prueba el rechazo (`IllegalArgumentException` con mensaje que contiene
    "minimum") de un monto de 200.00 PEN contra un mínimo de 500.00, coincide exactamente con la
    validación en `Auction.java:303-305`. La actualización de financiamiento con inversión válida
    ya estaba cubierta por `acceptsAQuoteAndAllocatesTheReceivableExactlyAcrossPartitions`
    (línea 49 y siguientes, preexistente).
  - Criterio 3 -> `AuctionFinancialFlowIntegrationTest#publishesPartitionAddedAndAuctionFullyFundedEventsWhenFundingCompletes`
    (líneas 175-208) usa `@RecordApplicationEvents`/`ApplicationEvents` para verificar 2
    `PartitionAddedEvent` (con `newCurrentFunding` final 9769.76) y 1 `AuctionFullyFundedEvent` al
    completar el monto objetivo, correspondiente a `Auction.java:342-348`.
  - Criterio 4 -> ver C7.
- C2: [ ] Razón: `rg -n 'com\.liquilabs\.vankoo\.investment\.(application|infrastructure|interfaces)' src/main/java/com/liquilabs/vankoo/investment/domain`
  devuelve un resultado: `src/main/java/com/liquilabs/vankoo/investment/domain/services/AuctionPricingCalculator.java:6`
  (`import com.liquilabs.vankoo.investment.infrastructure.configuration.PricingProperties;`).
  Confirmado con `git show develop:.../AuctionPricingCalculator.java` que este import ya existe
  en `develop` y no fue tocado por el diff de esta feature (`git diff develop...feature/add-auction-domain-test-baseline`
  solo modifica `feature_list.json`, `progress.md` y los dos archivos de test). Es deuda
  arquitectónica preexistente, fuera del alcance de la feature 1 (solo agrega tests, no toca
  `src/main`). No bloquea este veredicto; se recomienda registrarlo como una feature/bugfix
  separado en `feature_list.json`.
- C3: [x] Evidencia: `rg -n '\.(save|saveAll|delete|deleteAll|flush)\(' src/main/java/com/liquilabs/vankoo/investment/application/internal/queryservices`
  no devuelve resultados.
- C4: No aplica — la feature no agrega ni modifica controllers REST ni consumers Kafka; solo
  añade tests bajo `src/test`.
- C5: [x] Evidencia: `Auction.addInvestment` (dominio, `Auction.java:266-351`, sin modificar por
  esta feature) registra `PartitionAddedEvent` (línea 342) y, cuando `isFunded()`, también
  `AuctionFullyFundedEvent` (línea 348) desde el propio agregado antes de retornar. Los handlers
  de proyección en `application/internal/eventhandlers` no fueron tocados por este diff. La
  feature únicamente agrega cobertura de test sobre una transición ya existente y coherente con
  `docs/ARCHITECTURE.md`.
- C6: [x] Evidencia: `rg -n 'System\.out\.|TODO' src` no devuelve resultados.
- C7: [x] Evidencia: reejecutado de forma independiente por el reviewer (no solo confiando en
  `progress.md`):
  `$env:JAVA_HOME = "C:\Users\paulf\.jdks\openjdk-25"; & "C:\Users\paulf\.m2\wrapper\dists\apache-maven-3.9.10-bin\53h08a94dg6djh6umvruv7q564\apache-maven-3.9.10\bin\mvn.cmd" -q test`
  -> `EXIT_CODE=0`. Agregado de `target/surefire-reports/*.txt`: Tests run: 20, Failures: 0,
  Errors: 0, Skipped: 0. Coincide con lo reportado en la sección "Verificación" de este archivo.
- C8: [x] Evidencia: `git ls-files .env .env.* '*.pfx' '*.pem' '*.key'` no devuelve resultados.
- C9: [x] Evidencia: `grep -c '"status": "in_progress"' feature_list.json` = 1 (únicamente la
  feature 1). La feature sigue en `in_progress`, no se marcó `done` sin este review.

### Cambios requeridos

Ninguno. La feature 1 cumple sus 4 criterios de aceptación con evidencia verificable, no toca
código de producción (`src/main`), y `mvn test` termina en verde (20/20) verificado de forma
independiente por el reviewer. El único hallazgo (C2, import preexistente en
`AuctionPricingCalculator.java:6`) es deuda preexistente en `develop`, no introducida por esta
feature ni dentro de su alcance descrito en `feature_list.json`; se sugiere abrirlo como ítem de
backlog aparte en una sesión futura.
