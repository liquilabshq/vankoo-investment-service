# CHECKPOINTS.md — Criterios objetivos de aprobación

> El reviewer los evalúa junto con la aceptación de la feature. Todo checkpoint que no aplique
> requiere justificación explícita en `progress.md`; no se aprueba "a ojo".

- **C1 — Evidencia de aceptación.** `progress.md` enumera cada criterio de `acceptance`, la
  evidencia concreta y su resultado.
- **C2 — El dominio no depende de otras capas.** No hay imports desde `domain` hacia
  `application`, `infrastructure` o `interfaces`:

  ```bash
  rg -n 'com\.liquilabs\.vankoo\.investment\.(application|infrastructure|interfaces)' src/main/java/com/liquilabs/vankoo/investment/domain
  ```

  El comando no debe devolver resultados.
- **C3 — CQRS sin escrituras en queries.** Los query services no llaman métodos de escritura:

  ```bash
  rg -n '\.(save|saveAll|delete|deleteAll|flush)\(' src/main/java/com/liquilabs/vankoo/investment/application/internal/queryservices
  ```

  El comando no debe devolver resultados.
- **C4 — Adaptadores delgados.** Todo controller REST o consumer Kafka nuevo transforma su
  recurso/evento a un command o query y llama un servicio; no contiene reglas de negocio,
  transiciones de `Auction` ni acceso directo a repositorios. El reviewer debe citar el método
  inspeccionado como evidencia.
- **C5 — Eventos de dominio coherentes.** Una transición relevante nueva o modificada en un
  agregado registra un evento cuando otra parte del servicio debe reaccionar. Cada handler de
  proyección vive en `application/internal/eventhandlers` y no modifica el agregado. El reviewer
  debe comprobar el agregado y sus handlers afectados.
- **C6 — Convenciones y limpieza.** Se respeta `docs/CONVENTIONS.md`; además, el siguiente
  comando no devuelve resultados en código fuente:

  ```bash
  rg -n 'System\.out\.|TODO' src
  ```
- **C7 — Verificación automatizada.** `mvn test` finaliza con código de salida 0. Si Maven o el
  JDK requerido no están disponibles, la feature queda bloqueada y el entorno faltante se
  documenta; no se reemplaza por una aprobación sin pruebas.
- **C8 — Configuración segura.** No se versionan `.env` ni material de claves, y las nuevas
  credenciales, hosts o secretos se expresan mediante propiedades con variables de entorno.
  Verificar con:

  ```bash
  git ls-files .env .env.* '*.pfx' '*.pem' '*.key'
  ```

  El comando no debe devolver resultados.
- **C9 — Estado del harness.** Hay a lo sumo una feature `in_progress`; la feature no se marca
  `done` sin un review `APPROVED` registrado en `progress.md`.

## Formato del veredicto

El reviewer agrega al final de `progress.md` lo siguiente:

```markdown
## Review — feature <id>
**Veredicto:** APPROVED | CHANGES_REQUESTED

### Checkpoints
- C1: [x] Evidencia: ...
- C2: [ ] Razón: <archivo>:<línea> ...

### Cambios requeridos
1. ...
```
