# language: es
@EP02 @US08 @servicio_investment
Característica: Exploración del marketplace
  Como inversionista
  Quiero consultar y filtrar las facturas disponibles
  Para elegir oportunidades según riesgo, plazo, moneda y rendimiento

  Antecedentes:
    Dado que el inversionista inició sesión a través del API Gateway
    Y el marketplace contiene estas operaciones:
      | operación | estado       | moneda | clasificación | plazo en días | TEA inversionista | distintivo verde |
      | A         | PUBLISHED    | PEN    | A             | 30            | 12 %              | sí               |
      | B         | FUNDING      | PEN    | B             | 60            | 15 %              | no               |
      | C         | FUNDING      | USD    | C             | 90            | 18 %              | no               |
      | D         | FULLY_FUNDED | PEN    | A             | 45            | 12 %              | no               |

  Escenario: Mostrar por defecto solo las operaciones que aceptan inversiones
    Cuando el inversionista consulta el marketplace sin filtros
    Entonces el sistema devuelve las operaciones "A, B, C"
    Y cada operación muestra su clasificación, plazo, TEA y porcentaje de avance

  Esquema del escenario: Filtrar las operaciones disponibles
    Cuando el inversionista filtra el marketplace por <filtro>
    Entonces el sistema devuelve únicamente las operaciones "<resultado>"

    Ejemplos:
      | filtro                   | resultado |
      | moneda PEN               | A, B      |
      | moneda USD               | C         |
      | estado FULLY_FUNDED      | D         |
      | moneda PEN y solo verdes | A         |

  Escenario: Informar que no hay oportunidades coincidentes
    Cuando el inversionista filtra el marketplace por moneda USD y solo verdes
    Entonces el sistema devuelve una colección vacía
    Y informa que no existen oportunidades coincidentes

  Escenario: Rechazar una consulta con parámetros inválidos
    Cuando el inversionista pide una página de 101 operaciones
    Entonces el sistema responde con el código 400
    Y no devuelve resultados

  # Pendiente: la API filtra por estado, moneda y verde; el filtro por
  # clasificación de riesgo y por rango de plazo aún no está expuesto.
  @pending
  Escenario: Filtrar por clasificación de riesgo y plazo
    Cuando el inversionista filtra el marketplace por clasificación "A" y plazo de hasta 45 días
    Entonces el sistema devuelve únicamente las operaciones "A"
