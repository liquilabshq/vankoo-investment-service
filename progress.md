# progress.md — Estado de la sesión actual

> Se reinicia al cerrar cada sesión: el resumen se agrega a `history.md`.
> Si contiene una feature, notas o un review al iniciar, léelo antes de actuar.

## Feature en curso

- id 9 — fix-pricing-calculator-domain-layering-violation — Eliminar la dependencia de dominio
  hacia infraestructura en AuctionPricingCalculator.

## Rama

- bugfix/pricing-calculator-domain-leak (creada desde develop, aprobada explícitamente por el
  usuario con este nombre en lugar del formato `bugfix/<name>-9` propuesto por la skill
  create-git-branch).

## Plan

1. Crear `PricingParameters` como value object de dominio (`domain/model/valueobjects`) con
   únicamente los datos que `AuctionPricingCalculator` necesita: `version`, `dayCountBasis`,
   `platformMonthlyFeeRate`, `platformFeeTaxRate` y `investorTea` (con su método `teaFor`).
2. Modificar `AuctionPricingCalculator` para que deje de importar y recibir
   `PricingProperties`: se vuelve stateless y `calculate(...)` recibe `PricingParameters` como
   primer parámetro.
3. Actualizar `AuctionCommandServiceImpl.handle(CreateFinancialQuoteCommand)` para traducir
   `PricingProperties` a `PricingParameters` antes de invocar `pricingCalculator.calculate(...)`.
4. Actualizar `AuctionPricingCalculatorTest.properties()`/instanciación y
   `AuctionLifecycleTest` a la nueva firma del constructor y de `calculate(...)`.
5. Ejecutar `mvn test` y revisar los checkpoints C1, C2, C5, C6 y C9 antes de solicitar review.

## Notas de la sesión

- `mvn`/`mvnw` no estaban en el PATH del repo; se usó la misma distribución de Maven 3.9.10
  cacheada en `C:\Users\paulf\.m2\wrapper\dists\apache-maven-3.9.10-bin\...` junto con
  `JAVA_HOME=C:\Users\paulf\.jdks\openjdk-25` que se usó en la feature 1. No se modificó nada
  del repositorio para lograrlo.
- Alcance: solo se tocó `AuctionPricingCalculator`, el nuevo value object `PricingParameters`,
  el único call-site en `AuctionCommandServiceImpl` y los tres archivos de test que instancian
  el calculator (`AuctionPricingCalculatorTest`, `AuctionLifecycleTest`,
  `AuctionRepositoryTest`, esta última no mencionada en la descripción original de la feature
  pero rota por el cambio de firma si no se actualizaba). `PricingProperties` sigue existiendo
  sin cambios: `zoneId`, `quoteValidity`, `fundingWindow`, `settlementBuffer` y
  `minimumInvestment` siguen siendo configuración técnica consumida directamente por
  `AuctionCommandServiceImpl`/`AuctionQueryServiceImpl`, no por el calculator.

## Evidencia de aceptación

1. "`rg -n 'com\.liquilabs\.vankoo\.investment\.(application|infrastructure|interfaces)'
   src/main/java/com/liquilabs/vankoo/investment/domain` no devuelve resultados." -> Verificado,
   exit code 1 (sin coincidencias) tras eliminar el import de `PricingProperties` en
   `AuctionPricingCalculator.java`.
2. "AuctionPricingCalculator ya no importa ni recibe PricingProperties; recibe un value object
   de dominio con únicamente los datos de pricing que necesita." -> `AuctionPricingCalculator`
   (`domain/services/AuctionPricingCalculator.java`) quedó sin constructor propio (stateless) y
   su método `calculate` recibe `PricingParameters` (nuevo record en
   `domain/model/valueobjects/PricingParameters.java`) como primer parámetro, con `version`,
   `dayCountBasis`, `platformMonthlyFeeRate`, `platformFeeTaxRate` e `investorTea`/`teaFor`.
3. "La capa application (AuctionCommandServiceImpl) traduce PricingProperties al nuevo tipo de
   dominio antes de invocar AuctionPricingCalculator.calculate, sin duplicar reglas de
   negocio." -> `AuctionCommandServiceImpl.handle(CreateFinancialQuoteCommand)`
   (`application/internal/commandservices/AuctionCommandServiceImpl.java`) construye
   `PricingParameters` a partir de los accessors de `pricingProperties` justo antes de llamar a
   `pricingCalculator.calculate(...)`; no se duplica ninguna regla, solo se copian los 5 campos.
4. "AuctionPricingCalculatorTest y AuctionLifecycleTest se actualizan a la nueva firma y mvn
   test termina correctamente." -> `AuctionPricingCalculatorTest.properties()` se reemplazó por
   `AuctionPricingCalculatorTest.pricingParameters()` (devuelve `PricingParameters`); todos los
   call-sites de `calculator.calculate(...)` en `AuctionPricingCalculatorTest`,
   `AuctionLifecycleTest` y `AuctionRepositoryTest` (esta última también rota por el cambio de
   firma) ahora pasan `pricingParameters()` como primer argumento. Ver sección Verificación
   para `mvn test`.

## Verificación

- Comando: `mvn test` (Maven 3.9.10 cacheado localmente, JDK 25)
- Resultado: BUILD SUCCESS — Tests run: 20, Failures: 0, Errors: 0, Skipped: 0
  (`AuctionLifecycleTest` 8, `AuctionPricingCalculatorTest` 6, `AuctionRepositoryTest` 1,
  `InvoicingOcrEventToCommandAssemblerTest` 1, `AuctionFinancialFlowIntegrationTest` 4).
- Verificación manual: —

## Bloqueos

- Ninguno.

## Cierre

- Pendiente: reviewer y decisión de commits.

## Review — feature 9
**Veredicto:** APPROVED

### Checkpoints
- C1: [x] Evidencia: `progress.md` (secciones "Evidencia de aceptación" y "Verificación", líneas
  40-78) enumera los 4 criterios de `acceptance` de la feature 9 con evidencia concreta;
  verificado de forma independiente por el reviewer (ver C2, C7 abajo) y coincide con el estado
  real del código.
- C2: [x] Evidencia: `rg -n 'com\.liquilabs\.vankoo\.investment\.(application|infrastructure|interfaces)' src/main/java/com/liquilabs/vankoo/investment/domain`
  ejecutado por el reviewer, exit code 1 (sin coincidencias).
  `domain/services/AuctionPricingCalculator.java:1-16` solo importa
  `ch.obermuhlner.math.big.BigDecimalMath`, `domain/model/valueobjects/{Money,PricingParameters,
  ScoreGrade}` y `org.springframework.stereotype.Component`; ya no importa ni recibe
  `PricingProperties` (antes en el constructor). El nuevo value object
  `domain/model/valueobjects/PricingParameters.java:1-41` vive enteramente en `domain` y solo
  importa `java.math.BigDecimal`/`java.util.Map`.
- C3: [x] Evidencia: `rg -n '\.(save|saveAll|delete|deleteAll|flush)\(' src/main/java/com/liquilabs/vankoo/investment/application/internal/queryservices`
  ejecutado por el reviewer, exit code 1 (sin coincidencias). No aplica cambios de esta feature
  a query services; se confirma que la feature no introdujo regresión de CQRS.
- C4: [x] No aplica — la feature no crea ni modifica controllers REST ni consumers Kafka. Único
  call-site de aplicación afectado es `AuctionCommandServiceImpl.handle(CreateFinancialQuoteCommand)`
  (`application/internal/commandservices/AuctionCommandServiceImpl.java:95-107`), que solo
  traduce `pricingProperties` (5 campos: `version`, `dayCountBasis`, `platformMonthlyFeeRate`,
  `platformFeeTaxRate`, `investorTea`) a `PricingParameters` antes de invocar
  `pricingCalculator.calculate(...)`; no se duplica lógica de negocio, `PricingProperties` no
  se modificó (`infrastructure/configuration/PricingProperties.java`, sigue con sus 10 campos y
  `pricingZone()`/`teaFor()`/`minimumFor()` intactos).
- C5: [x] No aplica — la feature es una corrección de layering en un domain service sin
  agregado propio; no introduce ni modifica transiciones de `Auction` ni eventos de dominio.
  `Auction` y los handlers de `application/internal/eventhandlers` no aparecen en el diff
  (`git diff develop --stat`).
- C6: [x] Evidencia: `rg -n 'System\.out\.|TODO' src` ejecutado por el reviewer, exit code 1
  (sin coincidencias). Convenciones respetadas: `PricingParameters` es un record inmutable con
  validación en el compact constructor (`domain/model/valueobjects/PricingParameters.java:13-26`),
  igual estilo que `PricingProperties`; `AuctionPricingCalculator` quedó stateless (`@Component`
  sin constructor propio) y usa inyección por constructor en sus consumidores.
- C7: [x] Evidencia: `mvn test` ejecutado de forma independiente por el reviewer
  (`JAVA_HOME=C:\Users\paulf\.jdks\openjdk-25`, Maven 3.9.10 cacheado en
  `C:\Users\paulf\.m2\wrapper\dists\...`) — EXITCODE:0 (BUILD SUCCESS). Resumen de
  `target/surefire-reports/*.txt` recolectado tras la corrida: `AuctionLifecycleTest` 8/0/0/0,
  `AuctionPricingCalculatorTest` 6/0/0/0, `AuctionRepositoryTest` 1/0/0/0,
  `InvoicingOcrEventToCommandAssemblerTest` 1/0/0/0, `AuctionFinancialFlowIntegrationTest`
  4/0/0/0 (Tests run/Failures/Errors/Skipped) — Total: Tests run: 20, Failures: 0, Errors: 0,
  Skipped: 0. Coincide exactamente con lo registrado en la sección "Verificación" de este
  archivo.
- C8: [x] Evidencia: `git ls-files .env .env.* '*.pfx' '*.pem' '*.key'` ejecutado por el
  reviewer, exit code 0 y sin salida (no hay archivos versionados). El diff de esta feature no
  toca configuración, hosts ni credenciales.
- C9: [x] Evidencia: `rg -o '"status": "in_progress"' feature_list.json | wc -l` ejecutado por
  el reviewer -> `1` (solo la feature 9). La feature 9 permanece en `in_progress` en
  `feature_list.json` (no se marcó `done` antes de este review, cumpliendo la regla de
  `AGENTS.md`).

### Cambios requeridos
Ninguno.
