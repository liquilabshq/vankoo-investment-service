package com.liquilabs.vankoo.investment.interfaces.rest.controllers;

import com.liquilabs.vankoo.investment.domain.model.queries.AuctionMarketplaceView;
import com.liquilabs.vankoo.investment.domain.model.queries.GetMarketplaceAuctionsQuery;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionId;
import com.liquilabs.vankoo.investment.domain.services.AuctionCommandService;
import com.liquilabs.vankoo.investment.domain.services.AuctionQueryService;
import com.liquilabs.vankoo.investment.interfaces.rest.resources.CreateAuctionResource;
import com.liquilabs.vankoo.investment.interfaces.rest.resources.CreateInvestmentResource;
import com.liquilabs.vankoo.investment.interfaces.rest.transform.CreateAuctionCommandFromResourceAssembler;
import com.liquilabs.vankoo.investment.interfaces.rest.transform.CreatePartitionCommandFromResourceAssembler;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/v1/auctions")
@Tag(name = "Auctions", description = "Endpoints para la gestión de subastas e inversiones")
public class AuctionsController {

    private final AuctionCommandService auctionCommandService;
    private final AuctionQueryService auctionQueryService;

    public AuctionsController(AuctionCommandService auctionCommandService, AuctionQueryService auctionQueryService) {
        this.auctionCommandService = auctionCommandService;
        this.auctionQueryService = auctionQueryService;
    }

    @PostMapping
    @Operation(summary = "Crear una nueva subasta")
    public ResponseEntity<String> createAuction(@RequestBody CreateAuctionResource resource) {
        var command = CreateAuctionCommandFromResourceAssembler.toCommandFromResource(resource);
        var auctionId = auctionCommandService.handle(command);

        if (auctionId.isEmpty()) {
            return ResponseEntity.badRequest().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(auctionId.get().uuid());
    }

    @PostMapping("/{auctionId}/investments")
    @Operation(summary = "Invertir en una subasta")
    public ResponseEntity<String> invest(@PathVariable String auctionId,
                                         @Valid @RequestBody CreateInvestmentResource resource) {
        var command = CreatePartitionCommandFromResourceAssembler.toCommandFromResource(
                new AuctionId(auctionId), resource);
        var partitionId = auctionCommandService.handle(command);

        if (partitionId.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.status(HttpStatus.CREATED).body(partitionId.get().uuid());
    }

    @GetMapping("/marketplace")
    @Operation(summary = "Obtener el Marketplace")
    public ResponseEntity<List<AuctionMarketplaceView>> getMarketplaceAuctions() {
        var query = new GetMarketplaceAuctionsQuery(Optional.empty(), Optional.empty());
        var views = auctionQueryService.handle(query);

        return ResponseEntity.ok(views);
    }
}