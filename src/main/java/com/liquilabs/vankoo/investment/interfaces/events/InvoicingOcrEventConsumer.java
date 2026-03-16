package com.liquilabs.vankoo.investment.interfaces.events;

import com.liquilabs.vankoo.investment.domain.services.AuctionCommandService;
import com.liquilabs.vankoo.investment.interfaces.events.resources.InvoiceOcrProcessedIntegrationEvent;
import com.liquilabs.vankoo.investment.interfaces.events.transform.InvoicingOcrEventToCommandAssembler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Consumer;

@Configuration
public class InvoicingOcrEventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(InvoicingOcrEventConsumer.class);
    private final AuctionCommandService auctionCommandService;

    public InvoicingOcrEventConsumer(AuctionCommandService auctionCommandService) {
        this.auctionCommandService = auctionCommandService;
    }

    @Bean
    public Consumer<InvoiceOcrProcessedIntegrationEvent> processInvoicingOcrEvent() {
        return event -> {
            LOGGER.info("Evento OCR recibido desde Invoicing. Factura ID: {}", event.invoiceId());

            try {
                var command = InvoicingOcrEventToCommandAssembler.toCommandFromEvent(event);
                var auctionId = auctionCommandService.handle(command);

                auctionId.ifPresentOrElse(
                        id -> LOGGER.info("[Kafka] Subasta creada exitosamente. ID: {}", id.uuid()),
                        () -> LOGGER.error("[Kafka] Falló la creación de subasta para la factura: {}", event.invoiceId())
                );
            } catch (Exception e) {
                LOGGER.error("[Kafka] Error crítico al procesar el evento de Invoicing: {}", e.getMessage(), e);
            }
        };
    }
}