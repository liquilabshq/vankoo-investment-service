---
name: database-schema-change
description: Controlar cambios de entidades o esquema que puedan modificar una base de datos al ejecutar el servicio.
---

# Cambio de esquema de base de datos

Usa esta skill antes de modificar una entidad JPA, migración, relación o configuración que pueda
alterar el esquema. El perfil de desarrollo usa `spring.jpa.hibernate.ddl-auto=update`.

1. Identifica las tablas, columnas, datos existentes y entornos potencialmente afectados.
2. Muestra el cambio exacto y su efecto de esquema previsto; incluye cómo se validará o revertirá.
3. Espera aprobación explícita antes de editar la entidad o arrancar una aplicación conectada a
   una base de datos que pueda aplicar el cambio.
4. Tras aprobar, realiza únicamente el cambio aprobado y documenta el resultado en `progress.md`.

No ejecutes cambios destructivos, no dependas de `ddl-auto=update` como plan de migración de
producción y no uses credenciales reales en la salida.
