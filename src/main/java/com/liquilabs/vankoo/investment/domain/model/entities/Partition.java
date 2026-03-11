package com.liquilabs.vankoo.investment.domain.model.entities;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "partitions")
@Getter
@NoArgsConstructor
public class Partition {

    @EmbeddedId
    private PartitionId id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "uuid", column = @Column(name = "auction_id"))
    })
    private AuctionId auctionId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "uuid", column = @Column(name = "investor_id"))
    })
    private UserId investorId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "investment_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "investment_currency"))
    })
    private Money amount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "participation_percentage"))
    })
    private Percentage percentage;

    private LocalDateTime purchasedAt;

    @Enumerated(EnumType.STRING)
    private PartitionStatus status;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "expected_return_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "expected_return_currency"))
    })
    private Money expectedReturn;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "return_rate"))
    })
    private Percentage returnRate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "actual_return_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "actual_return_currency"))
    })
    private Money actualReturn;

    private LocalDateTime paidAt;
    private String investmentTransactionId;
    private String paymentTransactionId;

    public Partition(AuctionId auctionId, UserId investorId, Money amount, Percentage percentage, Percentage returnRate, String transactionId) {
        this.id = new PartitionId();
        this.auctionId = auctionId;
        this.investorId = investorId;
        this.amount = amount;
        this.percentage = percentage;
        this.returnRate = returnRate;
        this.purchasedAt = LocalDateTime.now();
        this.status = PartitionStatus.ACTIVE;
        this.investmentTransactionId = transactionId;
        this.expectedReturn = this.calculateReturn();
    }

    public Money calculateReturn() {
        Money profit = returnRate.of(amount);
        return amount.add(profit);
    }

    public void markAsPaid(String transactionId) {
        if (this.status != PartitionStatus.ACTIVE) {
            throw new IllegalStateException("Partition must be ACTIVE to be paid");
        }
        this.status = PartitionStatus.PAID;
        this.paidAt = LocalDateTime.now();
        this.paymentTransactionId = transactionId;
        this.actualReturn = this.expectedReturn;
    }

    public void markAsDefaulted() {
        this.status = PartitionStatus.DEFAULTED;
    }

    public void cancel() {
        this.status = PartitionStatus.CANCELLED;
    }
}