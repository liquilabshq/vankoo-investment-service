---
name: push-git-branch
description: Proponer el push de una rama de trabajo y ejecutarlo solo tras aprobación explícita.
---

# Publicar rama remota

Usa esta skill inmediatamente antes de enviar una rama al remoto.

1. Lee `progress.md`, `git status --short --branch` y el último commit de la rama actual.
2. Muestra el remoto, rama y comando exacto con la rama actual, por ejemplo
   `git push -u origin codex/add-auction-domain-test-baseline`.
3. Espera aprobación explícita de la persona usuaria.
4. Tras aprobar, ejecuta solo el comando mostrado y registra el resultado en `progress.md`.

No uses `--force`, no publiques otra rama y no combines el push con creación de pull request.
