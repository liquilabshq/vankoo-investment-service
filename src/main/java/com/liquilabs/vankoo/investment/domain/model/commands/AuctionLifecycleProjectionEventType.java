package com.liquilabs.vankoo.investment.domain.model.commands;

public enum AuctionLifecycleProjectionEventType {
    AuctionCreated,
    AuctionEvaluated,
    AuctionRejected,
    AuctionPublished,
    PartitionAdded,
    AuctionFullyFunded,
    AuctionExpired,
    AuctionCancelled,
    AuctionClosed
}
