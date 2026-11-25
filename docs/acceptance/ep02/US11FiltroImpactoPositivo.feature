# language: es
@EP02 @US11 @servicio_investment
Característica: Filtro de impacto positivo
  Como inversionista
  Quiero filtrar las facturas de impacto positivo
  Para concentrar mis inversiones en empresas sostenibles

  Escenario: Mostrar solo las operaciones con certificación verde
    Dado que el marketplace contiene estas operaciones:
      | operación | estado    | distintivo verde |
      | A         | publicada | sí               |
      | B         | en fondeo | no               |
      | C         | en fondeo | sí               |
      | D         | vencida   | sí               |
    Cuando el inversionista activa el filtro de impacto positivo
    Entonces el sistema devuelve únicamente las operaciones con certificación verde vigente:
      | operación |
      | A         |
      | C         |

  Escenario: Informar que no hay oportunidades sostenibles
    Dado que el marketplace contiene estas operaciones:
      | operación | estado    | distintivo verde |
      | E         | publicada | no               |
      | F         | vencida   | sí               |
    Cuando el inversionista aplica el filtro de impacto positivo
    Entonces el sistema devuelve una colección vacía
    Y informa que no hay oportunidades sostenibles disponibles
