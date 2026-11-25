# language: es
@EP01 @US06 @servicio_investment
Característica: Solicitud de subasta
  Como empresario MYPE
  Quiero enviar una factura elegible a subasta
  Para que los inversionistas puedan financiarla

  # La identidad de la MYPE la valida Invoicing con el KYC antes de admitir la factura:
  # toda factura que llega a Investment ya viene de una MYPE verificada.

  Escenario: Publicar una operación que cumple los requisitos
    Dado que la factura fue evaluada con riesgo "A" y su saldo sigue pendiente de cobro
    Y la MYPE tiene una cotización vigente del adelanto
    Cuando el empresario acepta la cotización para publicar la operación
    Entonces el sistema cambia su estado a "En subasta"
    Y abre la ventana de fondeo antes del vencimiento de la factura
    Y la incorpora al marketplace de oportunidades

  Esquema del escenario: Rechazar una publicación con requisitos pendientes
    Dado que la factura es elegible pero falta <requisito>
    Cuando el empresario acepta la cotización para publicar la operación
    Entonces el sistema rechaza la solicitud e informa el requisito pendiente
    Y la operación no se publica en el marketplace

    Ejemplos:
      | requisito                                      |
      | la evaluación de riesgo                        |
      | una cotización vigente                         |
      | un plazo suficiente antes del vencimiento      |
