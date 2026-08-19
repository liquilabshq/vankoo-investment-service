---
name: modify-ci-workflow
description: Proponer cambios a CI y ejecutarlos solo tras aprobación explícita por su impacto compartido.
---

# Modificar CI

Usa esta skill antes de editar archivos de CI, incluidos workflows de GitHub Actions o scripts
que se ejecuten en pipelines compartidos.

1. Identifica el archivo de CI, el trigger afectado, los permisos y los comandos que cambiarán.
2. Explica el impacto esperado y muestra el diff o plan exacto.
3. Espera aprobación explícita antes de editar o ejecutar comandos vinculados a CI.
4. Después de aprobar, aplica solo el cambio aprobado y registra la verificación disponible en
   `progress.md`.

No amplíes permisos, no desactives verificaciones ni cambies secretos sin aprobación específica.
