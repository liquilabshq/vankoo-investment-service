# language: es
@EP01 @US04 @servicio_investment
Característica: Firma digital de cesión
  Como empresario MYPE
  Quiero firmar digitalmente el contrato de cesión
  Para formalizar el adelanto de mi factura sin trámites físicos

  Escenario: Formalizar una operación con una firma válida
    Dado que el empresario tiene una identidad verificada
    Y ha aceptado la cotización de la factura
    Cuando completa la firma digital del contrato de cesión
    Entonces el sistema conserva el contrato firmado asociado a la operación
    Y cambia el estado de la operación a "Formalizada"

  Esquema del escenario: Bloquear una firma que no puede validarse
    Dado que el empresario ha aceptado la cotización de la factura
    Y la firma presenta <problema>
    Cuando intenta completar la firma digital del contrato de cesión
    Entonces el sistema mantiene la operación en "Pendiente de firma"
    Y no habilita el desembolso

    Ejemplos:
      | problema                                  |
      | un token de firma expirado                |
      | una identidad distinta de la verificada  |
