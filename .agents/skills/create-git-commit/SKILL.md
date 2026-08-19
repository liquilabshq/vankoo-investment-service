---
name: create-git-commit
description: Preparar cambios aprobados como commits convencionales seguros por archivo, con aprobación explícita previa.
---

# Commits convencionales por archivo

Usa esta skill después de un review `APPROVED` y antes de cerrar una feature.

1. Lee `progress.md`, `feature_list.json`, `git status --short` y `git diff --check`.
2. Confirma que el review de la única feature `in_progress` es `APPROVED` y separa los cambios
   de esa feature de cualquier cambio ajeno.
3. Excluye `.env`, `.env.*`, `*.env`, `*.pfx`, `*.pem`, `*.key`, `secrets.*` y
   `credentials.*`. También excluye cualquier archivo que contenga una credencial, token,
   contraseña o cadena de conexión real. Repórtalo sin stagearlo y pide una decisión explícita.
4. Para cada archivo seguro, propone exactamente un `git add -- ruta/archivo` y un commit con
   formato Conventional Commit: `feat`, `fix`, `docs`, `refactor`, `test`, `build`, `ci` o
   `chore`; el mensaje va en inglés y describe solo la contribución de ese archivo.
5. Muestra el plan completo y espera aprobación explícita. Tras aprobar, procesa cada archivo:
   stagea solo ese archivo, crea el commit con `git commit -m`, comprueba su stat y registra los
   hashes en `progress.md`.

No uses `git add .`, `git add -A`, `git commit -a`, `--amend`, `--no-verify`, `--author` ni
trailers de coautoría. No incluyas cambios no relacionados.
