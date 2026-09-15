package com.liquilabs.vankoo.investment.interfaces.rest.controllers;

import com.liquilabs.vankoo.investment.domain.exceptions.AuctionNotFoundException;
import com.liquilabs.vankoo.investment.domain.model.commands.*;
import com.liquilabs.vankoo.investment.domain.model.queries.GetAuctionByIdQuery;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionId;
import com.liquilabs.vankoo.investment.domain.services.AuctionCommandService;
import com.liquilabs.vankoo.investment.domain.services.AuctionQueryService;
import com.liquilabs.vankoo.investment.interfaces.rest.resources.*;
import com.liquilabs.vankoo.investment.interfaces.rest.transform.CreateAuctionCommandFromResourceAssembler;
import com.liquilabs.vankoo.investment.interfaces.rest.transform.CreatePartitionCommandFromResourceAssembler;
import com.liquilabs.vankoo.investment.interfaces.rest.transform.MarketplaceQueryAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/auctions")
@Tag(name = "Auctions", description = "Auction quotes, publication and fixed-rate investments")
public class AuctionsController {

    private final AuctionCommandService auctionCommandService;
    private final AuctionQueryService auctionQueryService;

    public AuctionsController(AuctionCommandService auctionCommandService, AuctionQueryService auctionQueryService) {
        this.auctionCommandService = auctionCommandService;
        this.auctionQueryService = auctionQueryService;
    }

    @PostMapping
    @Operation(summary = "Create an auction candidate")
    public ResponseEntity<String> createAuction(@Valid @RequestBody CreateAuctionResource resource) {
        var command = CreateAuctionCommandFromResourceAssembler.toCommandFromResource(resource);
        var auctionId = auctionCommandService.handle(command);
        return ResponseEntity.status(HttpStatus.CREATED).body(auctionId.uuid());
    }

    @GetMapping("/{auctionId}")
    @Operation(summary = "Get auction financial and lifecycle details")
    public AuctionDetailsResource getAuction(@PathVariable String auctionId) {
        return AuctionDetailsResource.from(findAuction(auctionId));
    }

    @PostMapping("/{auctionId}/quotes")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create a 24-hour financial quote for an evaluated auction")
    public FinancialQuoteResource createQuote(@PathVariable String auctionId) {
        var quote = auctionCommandService.handle(new CreateFinancialQuoteCommand(new AuctionId(auctionId)));
        return FinancialQuoteResource.from(quote);
    }

    @PostMapping("/{auctionId}/quotes/{quoteId}/accept")
    @Operation(summary = "Accept a financial quote and publish the auction")
    public AuctionDetailsResource acceptQuote(@PathVariable String auctionId, @PathVariable String quoteId) {
        var auction = auctionCommandService.handle(
                new AcceptFinancialQuoteCommand(new AuctionId(auctionId), quoteId)
        );
        return AuctionDetailsResource.from(auction);
    }

    @PostMapping("/{auctionId}/investments")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Buy a fixed-rate participation in a published auction")
    public InvestmentResponseResource invest(
            @PathVariable String auctionId,
            @Valid @RequestBody CreateInvestmentResource resource
    ) {
        var command = CreatePartitionCommandFromResourceAssembler.toCommandFromResource(
                new AuctionId(auctionId), resource
        );
        return InvestmentResponseResource.from(auctionCommandService.handle(command));
    }

    @PostMapping("/{auctionId}/cancel")
    @Operation(summary = "Cancel an auction that has no committed investments")
    public AuctionDetailsResource cancel(
            @PathVariable String auctionId,
            @Valid @RequestBody CancelAuctionResource resource
    ) {
        var auction = auctionCommandService.handle(
                new CancelAuctionCommand(new AuctionId(auctionId), resource.reason(), false)
        );
        return AuctionDetailsResource.from(auction);
    }

    @GetMapping("/marketplace")
    @Operation(summary = "List published auctions available for investment")
    public MarketplacePageResource getMarketplaceAuctions(
            @RequestParam(required = false) List<String> status,
            @RequestParam(required = false) String currency,
            @RequestParam(required = false) Boolean greenCertified,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "expiresAt,asc") String sort
    ) {
        var query = MarketplaceQueryAssembler.toQuery(
                status, currency, greenCertified, page, size, sort
        );
        return MarketplacePageResource.from(auctionQueryService.handle(query));
    }

    private com.liquilabs.vankoo.investment.domain.model.aggregates.Auction findAuction(String auctionId) {
        return auctionQueryService.handle(new GetAuctionByIdQuery(new AuctionId(auctionId)))
                .orElseThrow(() -> new AuctionNotFoundException(auctionId));
    }
}
