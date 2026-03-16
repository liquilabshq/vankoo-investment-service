package com.liquilabs.vankoo.investment.interfaces.events.resources;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record InvoiceOcrProcessedIntegrationEvent(
        @JsonProperty("EventId") UUID eventId,

        @JsonProperty("OccurredOn")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSSSSS'Z'")
        LocalDateTime occurredOn,

        @JsonProperty("InvoiceId") String invoiceId,
        @JsonProperty("MypeId") String mypeId,
        @JsonProperty("PayerRuc") String payerRuc,
        @JsonProperty("PayerName") String payerName,

        @JsonProperty("DueDate")
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss'Z'")
        LocalDateTime dueDate,

        @JsonProperty("Currency") String currency,
        @JsonProperty("TotalAmount") BigDecimal totalAmount
) {}