# language: es
@NFR @NFR02 @integridad @servicio_investment
Característica: Integridad del fondeo ante compras simultáneas
  Como empresario MYPE
  Quiero que mi factura nunca reciba más dinero del que solicité
  Para que el fondeo coincida exactamente con el monto de la operación

  # Atributo de calidad 2.1.1: Integridad de datos. Medida: 0 casos de sobre-fondeo.
  # La subasta se bloquea con PESSIMISTIC_WRITE mientras se registra cada participación.

  Antecedentes:
    Dado que la operación está en estado "FUNDING" con un saldo pendiente de 1000.00 PEN

  Escenario: Aceptar solo las compras que caben en el saldo pendiente
    Dado que estos inversionistas tienen saldo suficiente en su billetera:
      | inversionista | monto  |
      | inv-01        | 600.00 |
      | inv-02        | 600.00 |
      | inv-03        | 400.00 |
    Cuando los tres confirman su participación al mismo tiempo
    Entonces la suma de las participaciones aceptadas no supera 1000.00 PEN
    Y cada participación rechazada informa que el monto supera el saldo pendiente
    Y solo se debita la billetera de los inversionistas aceptados

  Escenario: Repetir la prueba de carga concurrente sin sobre-fondeo
    Dado que 50 inversionistas intentan invertir 100.00 PEN cada uno en paralelo
    Cuando termina el procesamiento de todas las solicitudes
    Entonces el monto financiado es exactamente 1000.00 PEN
    Y se aceptaron 10 participaciones
    Y la operación cambia su estado a "FULLY_FUNDED"
