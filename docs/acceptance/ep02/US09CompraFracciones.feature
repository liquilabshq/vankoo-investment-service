# language: es
@EP02 @US09 @servicio_investment
Característica: Compra de fracciones
  Como inversionista
  Quiero comprar una fracción de una factura
  Para diversificar mi riesgo con montos bajos

  Antecedentes:
    Dado que la operación tiene estos datos de fondeo:
      | estado  | moneda | monto objetivo | monto financiado | monto mínimo de inversión |
      | FUNDING | PEN    | 10000.00       | 7000.00          | 50.00                     |
    Y la ventana de fondeo sigue abierta

  Escenario: Registrar una participación válida
    Dado que el inversionista tiene un saldo de 2000.00 PEN en su billetera
    Cuando confirma una participación de 1000.00 PEN
    Entonces el sistema registra la participación a nombre del inversionista
    Y descuenta 1000.00 PEN de su billetera
    Y la operación queda con este avance:
      | monto financiado | saldo pendiente | avance |
      | 8000.00          | 2000.00         | 80 %   |

  Escenario: Completar el fondeo con el saldo exacto pendiente
    Dado que el inversionista tiene un saldo de 5000.00 PEN en su billetera
    Cuando confirma una participación de 3000.00 PEN
    Entonces la operación cambia su estado a "FULLY_FUNDED"

  Esquema del escenario: Rechazar una participación inválida sin mover dinero
    Dado que el inversionista tiene un saldo de <saldo> PEN en su billetera
    Cuando confirma una participación de <monto> <moneda>
    Entonces el sistema rechaza la participación por "<motivo>"
    Y el saldo de la billetera sigue siendo <saldo> PEN
    Y el monto financiado sigue siendo 7000.00 PEN

    Ejemplos:
      | saldo   | monto   | moneda | motivo                               |
      | 500.00  | 1000.00 | PEN    | saldo insuficiente                   |
      | 5000.00 | 3500.00 | PEN    | el monto supera el saldo pendiente   |
      | 5000.00 | 20.00   | PEN    | el monto es menor que el mínimo      |
      | 5000.00 | 100.00  | USD    | la moneda no coincide con la subasta |

  Escenario: No cobrar dos veces una participación repetida
    Dado que el inversionista tiene un saldo de 2000.00 PEN en su billetera
    Y ya confirmó una participación de 1000.00 PEN con la transacción "tx-001"
    Cuando repite la participación con la misma transacción "tx-001"
    Entonces el sistema no registra una segunda participación
    Y el saldo de la billetera sigue siendo 1000.00 PEN
