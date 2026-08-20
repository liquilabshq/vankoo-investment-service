package com.liquilabs.vankoo.investment.domain.model.aggregates;

import com.liquilabs.vankoo.investment.domain.model.entities.Partition;
import com.liquilabs.vankoo.investment.domain.model.events.AuctionCreatedEvent;
import com.liquilabs.vankoo.investment.domain.model.events.AuctionFullyFundedEvent;
import com.liquilabs.vankoo.investment.domain.model.events.PartitionAddedEvent;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.*;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.domain.AbstractAggregateRoot;
import org.springframework.data.domain.Persistable;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static java.math.BigDecimal.ZERO;

@Entity
@Getter
@NoArgsConstructor
@EntityListeners(AuditingEntityListener.class)
public class Auction extends AbstractAggregateRoot<Auction> implements Persistable<AuctionId> {

    @EmbeddedId
    private AuctionId id;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "uuid", column = @Column(name = "invoice_id", unique = true))
    })
    private InvoiceId invoiceId;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "uuid", column = @Column(name = "mype_id"))
    })
    private UserId mypeId;

    @Enumerated(EnumType.STRING)
    private AuctionStatus status;

    @Embedded
    private RiskScore riskScore;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "invoice_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "invoice_currency"))
    })
    private Money invoiceAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "discount_rate"))
    })
    private Percentage discountRate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "net_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "net_currency"))
    })
    private Money netAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "value", column = @Column(name = "commission_rate"))
    })
    private Percentage commissionRate;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "target_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "target_currency"))
    })
    private Money targetAmount;

    @Embedded
    @AttributeOverrides({
            @AttributeOverride(name = "amount", column = @Column(name = "current_funding_amount")),
            @AttributeOverride(name = "currency", column = @Column(name = "current_funding_currency"))
    })
    private Money currentFunding;

    // Spring llena esto automáticamente
    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;


    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime closedAt;

    private LocalDateTime dueDate;

    private boolean greenCertified;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "auction_id")
    private List<Partition> partitions = new ArrayList<>();

    // Constructor limpio de negocio
    public Auction(InvoiceId invoiceId, UserId mypeId, Money invoiceAmount, RiskScore riskScore, boolean greenCertified, LocalDateTime dueDate) {
        this.id = new AuctionId();
        this.invoiceId = invoiceId;
        this.mypeId = mypeId;
        this.invoiceAmount = invoiceAmount;
        this.riskScore = riskScore;
        this.greenCertified = greenCertified;
        this.dueDate = dueDate;
        this.status = AuctionStatus.PENDING_VERIFICATION_RISK;
        this.currentFunding = new Money(ZERO, invoiceAmount.currency());
    }

    // MÉTODOS DE EVENTOS

    public void registerAuctionCreatedEvent(String payerRuc, String payerName) {
        this.registerEvent(new AuctionCreatedEvent(
                this.id.uuid(),
                this.invoiceId.uuid(),
                this.mypeId.uuid(),
                payerRuc,
                payerName,
                this.dueDate,
                this.invoiceAmount.amount(),
                this.invoiceAmount.currency().name(),
                this.status,
                this.greenCertified
        ));
    }

    // MÉTODOS DE PERSISTABLE

    @Override
    public AuctionId getId() {
        return this.id;
    }

    @Override
    public boolean isNew() {
        return this.createdAt == null;
    }

    // MÉTODOS DE NEGOCIO

    public void calculateFinancials(Percentage discount, Percentage commission) {
        this.discountRate = discount;
        this.commissionRate = commission;

        Money discountValue = discount.of(invoiceAmount);
        this.netAmount = invoiceAmount.subtract(discountValue);

        Money commissionValue = commission.of(invoiceAmount);
        this.targetAmount = netAmount.subtract(commissionValue);
    }

    public void publish(LocalDateTime expirationDate) {
        if (this.status != AuctionStatus.PENDING_VERIFICATION_RISK && this.status != AuctionStatus.DRAFT) {
            throw new IllegalStateException("Auction cannot be published from current status");
        }
        this.status = AuctionStatus.PUBLISHED;
        this.publishedAt = LocalDateTime.now();
        this.expiresAt = expirationDate;
    }

    public boolean canAcceptPartition(Money amount) {
        if (this.status != AuctionStatus.PUBLISHED && this.status != AuctionStatus.FUNDING) {
            return false;
        }
        Money projectedFunding = this.currentFunding.add(amount);
        return !projectedFunding.isGreaterThan(this.targetAmount);
    }

    private static final BigDecimal MINIMUM_INVESTMENT_AMOUNT = new BigDecimal("500.00");

    private Percentage calculateParticipationPercentage(Money investmentAmount) {
        BigDecimal percentageValue = investmentAmount.amount()
                .divide(this.targetAmount.amount(), 4, java.math.RoundingMode.HALF_UP)
                .multiply(new BigDecimal("100"));
        return new Percentage(percentageValue);
    }

    public void addInvestment(UserId investorId, Money amount, Percentage returnRate, String transactionId) {
        // Regla 1: Validar el ticket mínimo (Ejemplo: S/ 500)
        if (amount.amount().compareTo(MINIMUM_INVESTMENT_AMOUNT) < 0) {
            throw new IllegalArgumentException("El monto de inversión debe ser de al menos " + MINIMUM_INVESTMENT_AMOUNT);
        }

        // Regla 2: Validar que la subasta acepte este monto (no exceder el total)
        if (!canAcceptPartition(amount)) {
            throw new IllegalArgumentException("El monto excede lo que falta fondear o la subasta no está activa");
        }

        // Regla 3: El Dominio calcula el porcentaje real y exacto
        Percentage calculatedPercentage = calculateParticipationPercentage(amount);

        // Regla 4: El Aggregate Root (Auction) crea a su entidad hija (Partition)
        Partition newPartition = new Partition(
                this.id,
                investorId,
                amount,
                calculatedPercentage,
                returnRate,
                transactionId
        );

        // Agregamos y actualizamos totales
        this.partitions.add(newPartition);
        this.currentFunding = this.currentFunding.add(amount);
        this.status = AuctionStatus.FUNDING;

        // Lanzamos el evento para actualizar la barra de progreso en el Frontend
        this.registerEvent(new PartitionAddedEvent(
                this.id.uuid(),
                newPartition.getId().uuid(),
                amount.amount(),
                this.currentFunding.amount()
        ));

        // Cerramos si se completó
        if (isFunded()) {
            this.status = AuctionStatus.FULLY_FUNDED;
            this.registerEvent(new AuctionFullyFundedEvent(this.id.uuid()));
        }
    }

    public boolean isFunded() {
        return !this.currentFunding.isLessThan(this.targetAmount);
    }

    public void close() {
        this.status = AuctionStatus.CLOSED;
        this.closedAt = LocalDateTime.now();
    }

    public void cancel() {
        this.status = AuctionStatus.CANCELLED;
    }
}