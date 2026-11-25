# language: es
@EP01 @US07 @servicio_investment
Característica: Seguimiento del fondeo
  Como empresario MYPE
  Quiero conocer el avance del fondeo de mi factura
  Para saber cuándo se completa y cuándo recibiré el adelanto

  Escenario: Actualizar el avance al registrar una participación
    Dado que la operación está publicada en subasta
    Y todavía tiene un saldo pendiente de fondeo
    Cuando se registra una nueva participación de un inversionista
    Entonces el sistema actualiza el monto financiado
    Y muestra el porcentaje de avance y el saldo pendiente

  # Pendiente: el dominio pasa la subasta a FULLY_FUNDED, pero todavía no modela
  # la fecha estimada de transferencia del adelanto que pide el último paso.
  @pending
  Escenario: Informar que el fondeo se completó
    Dado que las participaciones registradas alcanzan el 100 % del monto objetivo
    Cuando el sistema confirma el fondeo completo de la operación
    Entonces cambia su estado a "Fondeada"
    Y registra la fecha estimada de transferencia del adelanto
