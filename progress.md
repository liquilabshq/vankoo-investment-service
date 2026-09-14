# progress.md — Estado de la sesión actual

> Se reinicia al cerrar cada sesión: el resumen se agrega a `history.md`.
> Si contiene una feature, notas o un review al iniciar, léelo antes de actuar.

## Feature en curso

- Feature 2 — `define-marketplace-read-model-boundary`.

## Rama

- `feature/investment-outbox-events`.

## Plan

1. Documentar propiedad, almacenamiento y API del read model Marketplace.
2. Congelar visibilidad, filtros, orden, paginación y frescura admitida.
3. Definir topic, clave, retención, replay y evolución de contratos.
4. Verificar aceptación documental y solicitar revisión independiente.

## Notas de la sesión

- La persona usuaria asignó a esta rama el track Dev 2 en el orden 2 → 4 → 5 → 6 → 7.
- Excepción autorizada al orden ascendente global: cada rama de desarrollador procesa una sola
  feature asignada a la vez; `feature_list.json` y `progress.md` se actualizarán secuencialmente
  dentro de esta rama.
- La persona usuaria decidió que `investment-service` conservará la propiedad del Marketplace,
  que se usará un topic único y que la caché admitirá hasta 30 segundos de frescura.
- La primera revisión detectó una dependencia preexistente desde `AuctionPricingCalculator` hacia
  infraestructura. Se introdujo `AuctionPricingPolicy` en dominio para satisfacer C2 sin alterar
  las fórmulas ni la configuración financiera.

## Evidencia de aceptación

- [x] Propiedad, almacenamiento y endpoint decididos: `docs/MARKETPLACE_READ_MODEL.md`,
  secciones `Ownership and boundary` y `Query contract`.
- [x] Campos, estados visibles, orden, filtros, paginación y frescura documentados:
  `docs/MARKETPLACE_READ_MODEL.md`, secciones `Visible data` y `Query contract`.
- [x] Replay sin eventos de dominio documentado: `docs/MARKETPLACE_READ_MODEL.md`, sección
  `Replay`.
- [x] Consumer, topic, key, versión, retención y evolución definidos:
  `docs/MARKETPLACE_READ_MODEL.md`, sección `Event stream`.

## Verificación

- Comando: `mvn -o "-Dmaven.repo.local=C:\Users\user\.m2\repository" test`
- Resultado: OK — 17 pruebas, 0 fallos, 0 errores.
- Verificación manual: `git diff --check` sin errores; decisiones contrastadas con los cuatro
  criterios de aceptación de la feature 2; C2 ya no devuelve imports desde dominio hacia
  `application`, `infrastructure` o `interfaces`.

## Bloqueos

- Ninguno para la feature 2; las decisiones que la mantenían bloqueada quedaron resueltas.

## Cierre

- Pendiente: feature activa, reviewer y decisión de commits.

## Review — feature 2
**Veredicto:** CHANGES_REQUESTED

### Checkpoints
- C1: [x] Evidencia: los cuatro criterios de aceptación están enumerados con referencias a
  `docs/MARKETPLACE_READ_MODEL.md`; propiedad y límite en líneas 3-11, datos visibles en líneas
  13-27, consulta en líneas 29-39, stream en líneas 41-58 y replay en líneas 60-70.
- C2: [ ] Razón: `src/main/java/com/liquilabs/vankoo/investment/domain/services/AuctionPricingCalculator.java:6`
  importa `infrastructure.configuration.PricingProperties`; el comando obligatorio devuelve un
  resultado, aunque la dependencia ya existía antes de esta feature documental.
- C3: [x] Evidencia: el `rg` obligatorio no encontró escrituras en query services.
- C4: [x] No aplica: la feature 2 no agrega ni modifica controllers REST o consumers Kafka.
- C5: [x] No aplica: la feature 2 no modifica el agregado ni handlers de eventos; documenta que la
  futura proyección consumirá contratos versionados y no eventos de dominio.
- C6: [x] Evidencia: `git diff --check` terminó con salida 0 y el `rg` obligatorio no encontró
  `System.out` ni `TODO` en `src`.
- C7: [x] Evidencia: `mvn -o "-Dmaven.repo.local=C:\Users\user\.m2\repository" test` terminó con
  `BUILD SUCCESS`; 17 pruebas, 0 fallos y 0 errores.
- C8: [x] Evidencia: `git ls-files -- ".env" ".env.*" "*.pfx" "*.pem" "*.key"` no devolvió
  archivos versionados.
- C9: [x] Evidencia: `feature_list.json` contiene una sola feature `in_progress` (feature 2) y no
  está marcada `done`.

### Cambios requeridos
1. Corregir la dependencia desde dominio hacia infraestructura detectada por C2 y volver a ejecutar
   todos los checkpoints antes de solicitar una nueva revisión.

## Review — feature 2 (segunda revisión)
**Veredicto:** APPROVED

### Checkpoints
- C1: [x] Evidencia: los cuatro criterios de aceptación permanecen documentados en
  `docs/MARKETPLACE_READ_MODEL.md`: propiedad y límite en líneas 3-11, datos visibles en líneas
  13-27, consulta en líneas 29-39, stream en líneas 41-58 y replay en líneas 60-70.
- C2: [x] Evidencia: el `rg` obligatorio no encontró dependencias desde dominio hacia
  `application`, `infrastructure` o `interfaces`; `AuctionPricingCalculator` ahora depende del
  contrato de dominio `AuctionPricingPolicy`, implementado por `PricingProperties`.
- C3: [x] Evidencia: el `rg` obligatorio no encontró escrituras en query services.
- C4: [x] No aplica: la feature 2 no agrega ni modifica controllers REST o consumers Kafka.
- C5: [x] No aplica: la feature 2 no modifica el agregado ni handlers de eventos; la documentación
  mantiene separados los eventos de dominio y los contratos de integración.
- C6: [x] Evidencia: `git diff --check` terminó con salida 0 y el `rg` obligatorio no encontró
  `System.out` ni `TODO` en `src`.
- C7: [x] Evidencia: `mvn -o "-Dmaven.repo.local=C:\Users\user\.m2\repository" test` terminó con
  `BUILD SUCCESS`; 17 pruebas, 0 fallos y 0 errores.
- C8: [x] Evidencia: `git ls-files -- ".env" ".env.*" "*.pfx" "*.pem" "*.key"` no devolvió
  archivos versionados.
- C9: [x] Evidencia: `feature_list.json` contiene únicamente la feature 2 como `in_progress` y no
  está marcada `done`.

### Cambios requeridos
1. Ninguno.
