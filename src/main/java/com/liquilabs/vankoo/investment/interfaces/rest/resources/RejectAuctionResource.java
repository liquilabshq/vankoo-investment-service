package com.liquilabs.vankoo.investment.interfaces.rest.resources;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;

import java.time.Instant;

public record RejectAuctionResource(
        @NotBlank @Size(max = 100) String assessmentId,
        @NotBlank @Size(max = 60) String reasonCode,
        @NotBlank @Size(max = 500) String reason,
        @NotNull @PastOrPresent Instant assessedAt
) {
}
