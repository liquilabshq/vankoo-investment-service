package com.liquilabs.vankoo.investment.interfaces.events.transform;

import com.liquilabs.vankoo.investment.domain.model.commands.CreateAuctionCommand;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.*;
import com.liquilabs.vankoo.investment.interfaces.events.resources.InvoiceOcrProcessedIntegrationEvent;

public class InvoicingOcrEventToCommandAssembler {

    public static CreateAuctionCommand toCommandFromEvent(InvoiceOcrProcessedIntegrationEvent event) {
        return new CreateAuctionCommand(
                new InvoiceId(event.invoiceId()),
                new UserId(event.mypeId()),
                new Money(event.totalAmount(), Currency.valueOf(event.currency())),
                RiskScore.pendingEvaluation(),
                false,
                event.payerRuc(),
                event.payerName(),
                event.dueDate()
        );
    }
}