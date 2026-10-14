---
name: create-git-branch
description: Proponer una rama Gitflow en inglés para una feature o bugfix y crearla solo tras aprobación explícita.
---

# Crear rama Gitflow

Usa esta skill inmediatamente antes de cambiar el estado de una feature o editar código.

1. Lee `feature_list.json`, `progress.md` y `git status --short --branch`.
2. Confirma que la feature es la única `pending` elegida, que no hay otra `in_progress` y que el
   árbol está limpio. Si no lo está, explica qué archivos bloquean el flujo y espera decisión.
3. Clasifica la tarea: `feature` para capacidad nueva o `bugfix` para corregir comportamiento.
   Obtén un contexto inglés breve del campo `name`, en kebab-case.
4. Propón una rama con formato `feature/contexto-en-ingles-id` o
   `bugfix/contexto-en-ingles-id` y muestra
   los tres comandos exactos desde la raíz:

   ```powershell
   git switch develop
   git pull origin develop
   git switch -c feature/add-auction-domain-test-baseline-1
   ```

5. Espera aprobación explícita antes de ejecutar cualquiera de esos comandos. Tras aprobar,
   ejecuta solo la secuencia mostrada, registra la rama en `progress.md` y después marca la
   feature `in_progress`.

No uses opciones force. Si `develop` no existe, el pull falla o la rama ya existe, detente y
espera instrucciones; no sustituyas el flujo por otra base.
