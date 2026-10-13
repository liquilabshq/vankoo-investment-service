# language: es
@EP01 @US03 @servicio_investment
Característica: Simulación de costos y adelanto
  Como empresario MYPE
  Quiero simular el costo de adelantar una factura
  Para conocer el monto que recibiré antes de aceptar la operación

  Escenario: Consultar las condiciones de una operación válida
    Dado que la factura tiene monto, moneda, plazo y clasificación de riesgo válidos
    Cuando el empresario solicita la simulación del adelanto
    Entonces el sistema informa el monto bruto, el descuento y la comisión
    Y muestra el monto neto que recibiría el empresario
    Y muestra el importe estimado para el inversionista

  Esquema del escenario: Rechazar una simulación con datos incompletos o fuera de rango
    Dado que la factura tiene <problema>
    Cuando el empresario solicita la simulación del adelanto
    Entonces el sistema rechaza la simulación e informa el dato que debe corregirse
    Y no permite crear una subasta con esos datos

    Ejemplos:
      | problema                                |
      | una clasificación de riesgo ausente     |
      | un monto fuera del rango permitido      |
      | un plazo fuera del rango permitido      |
