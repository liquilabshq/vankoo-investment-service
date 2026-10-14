# AGENTS.md — Mapa de navegación para agentes

> Este archivo es el punto de entrada del harness de `vankoo-investment-service`. Es un mapa:
> carga cada documento cuando corresponda, no como sustituto de leer el código afectado.

## Antes de empezar (obligatorio)

1. Lee `progress.md` y después `feature_list.json`.
2. Comprueba que no exista otra feature con estado `in_progress` y elige una sola `pending`, en
   orden ascendente de `id`.
3. Antes de modificar código o el estado de esa feature, usa
   `.agents/skills/create-git-branch/SKILL.md`: propone la rama Gitflow y espera aprobación
   explícita de la persona usuaria antes de ejecutar comandos Git.
4. Antes de implementar, lee `docs/ARCHITECTURE.md` y `docs/CONVENTIONS.md`. Consulta
   `CHECKPOINTS.md` antes de solicitar revisión y `docs/verification.md` cuando aplique.

## Mapa del repositorio

| Archivo o directorio | Contenido | Cuándo consultarlo |
| --- | --- | --- |
| `feature_list.json` | Backlog y máquina de estados | Al iniciar y al cambiar de estado |
| `progress.md` | Bitácora efímera de la sesión activa | Siempre al iniciar y durante el trabajo |
| `history.md` | Historial append-only de sesiones cerradas | Cuando falte contexto histórico |
| `CHECKPOINTS.md` | Criterios de aprobación verificables | Antes de solicitar revisión |
| `docs/ARCHITECTURE.md` | Capas, CQRS, agregados y eventos de dominio | Antes de cambiar código |
| `docs/CONVENTIONS.md` | Convenciones de código y del dominio | Antes de escribir código |
| `docs/verification.md` | Comando de verificación y prerequisitos | Al validar una feature |
| `.claude/agents/` | Roles leader, implementer y reviewer | Al orquestar subagentes |
| `.agents/skills/create-git-branch/` | Propuesta de rama Gitflow desde `develop` | Antes de cada feature o bugfix |
| `.agents/skills/create-git-commit/` | Plan de commits convencionales seguros por archivo | Al cerrar una feature aprobada |
| `.agents/skills/push-git-branch/` | Publicación de una rama remota aprobada | Antes de `git push` |
| `.agents/skills/database-schema-change/` | Protección de cambios JPA que afectan esquema | Antes de editar entidades o arrancar contra una BD afectable |
| `.agents/skills/modify-ci-workflow/` | Protección de cambios de CI | Antes de editar workflows o scripts de pipeline |

## Reglas duras

- Una sola feature puede estar `in_progress` en todo momento.
- No mezcles cambios de dos features, incluso si ambas parecen pequeñas.
- Respeta la dirección de dependencias y el modelo CQRS descritos en
  `docs/ARCHITECTURE.md`.
- La lógica de negocio y las transiciones de estado viven en el agregado; los adaptadores
  REST y Kafka solo transforman la entrada y delegan.
- Toda mutación relevante del agregado debe evaluar si expresa un evento de dominio. Un evento
  no se publica fuera del servicio sin un contrato de integración y una decisión explícita.
- No marques una feature como `done` sin un veredicto `APPROVED`, evidencia de aceptación
  en `progress.md` y una verificación satisfactoria.
- Documenta decisiones, comandos y bloqueos en `progress.md` mientras ocurren.
- No abras, imprimas ni agregues archivos `.env`, `.env.*`, `*.pfx`, `*.pem` o `*.key`. No cites
  credenciales ni valores de secretos de la configuración en archivos, commits o respuestas.
- No dejes `TODO` sin una feature asociada, `System.out` ni cambios de depuración.

## Ciclo de una feature

1. Selecciona una feature `pending` y solicita una rama mediante
   `.agents/skills/create-git-branch/SKILL.md`. La rama parte del `develop` actualizado y usa
   `feature/<contexto-en-inglés>-<id>` o `bugfix/<contexto-en-inglés>-<id>`.
2. Tras la aprobación, el `git pull` y la creación exitosa de la rama, cambia solo esa feature a
   `in_progress`.
3. Registra en `progress.md` la feature, rama y un plan de 3 a 5 puntos.
4. Implementa únicamente lo descrito en `description` y `acceptance`.
5. Ejecuta la verificación definida en `docs/verification.md` y registra resultados y
   evidencia de aceptación.
6. Solicita la revisión contra `CHECKPOINTS.md`. El reviewer agrega su veredicto a
   `progress.md`.
7. Con `APPROVED`, usa `.agents/skills/create-git-commit/SKILL.md` para proponer commits
   convencionales seguros por archivo. Solo tras los commits aprobados —o una decisión explícita
   de no hacerlos— marca la feature como `done`, archiva el resumen en `history.md` y reinicia
   `progress.md` con su plantilla.

## Bloqueos y cierre

Si falta una decisión de dominio, un servicio externo o una credencial, cambia la feature a
`blocked`, registra el hecho concreto y detén el trabajo. No inventes un workaround.

Al cerrar una sesión sin terminar, deja `progress.md` actualizado. Al cerrar una sesión
terminada, agrega una nueva entrada a `history.md`; no alteres entradas históricas.

## Orquestación

- `leader` descompone y coordina; nunca modifica código, el backlog ni un review.
- `implementer` completa exactamente una feature y nunca se la aprueba a sí mismo.
- `reviewer` solo inspecciona y emite un veredicto contra los checkpoints; nunca edita.

Los agentes comparten contexto mediante `feature_list.json` y `progress.md`; sus respuestas
en chat deben limitarse a una línea que remita a esos archivos.
