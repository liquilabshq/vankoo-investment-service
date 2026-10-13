# language: es
@EP01 @US06 @servicio_investment
Característica: Solicitud de subasta
  Como empresario MYPE
  Quiero enviar una factura elegible a subasta
  Para que los inversionistas puedan financiarla

  Escenario: Publicar una operación que cumple los requisitos
    Dado que la factura está validada y es elegible
    Y la identidad del empresario está aprobada
    Y existe una cuenta CCI registrada
    Y el contrato de cesión está formalizado
    Cuando el empresario solicita publicar la operación
    Entonces el sistema cambia su estado a "En subasta"
    Y la incorpora al marketplace de oportunidades

  Esquema del escenario: Rechazar una publicación con requisitos pendientes
    Dado que la factura es elegible pero falta <requisito>
    Cuando el empresario solicita publicar la operación
    Entonces el sistema rechaza la solicitud e informa el requisito pendiente
    Y no crea una subasta

    Ejemplos:
      | requisito                         |
      | la aprobación de identidad        |
      | una cuenta CCI registrada         |
      | un dato obligatorio de la factura |
