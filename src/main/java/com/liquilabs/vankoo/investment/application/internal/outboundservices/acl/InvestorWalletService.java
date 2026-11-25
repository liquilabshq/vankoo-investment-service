package com.liquilabs.vankoo.investment.application.internal.outboundservices.acl;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.Money;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.UserId;

/**
 * What Investment needs from the investor's wallet: take the money for a share,
 * before the share is granted.
 *
 * <p>The port is Investment's; Finance is behind it
 * ({@code FinanceInvestorWalletService}). Until now the mobile app debited the
 * wallet itself and then bought the share, which let a client skip the debit or
 * buy with someone else's id. Paying is now part of buying.
 */
public interface InvestorWalletService {

    /**
     * Debits {@code amount} from the investor's wallet as an {@code INVERSION}.
     *
     * <p>Idempotent on {@code idempotencyKey}: the same key and amount debit
     * once, however many times this is called. Investment passes the
     * investment's {@code transactionId}, so a client retrying a purchase that
     * timed out is never charged twice.
     *
     * @throws com.liquilabs.vankoo.investment.domain.exceptions.InsufficientBalanceException
     *         when the wallet does not cover the amount, or was never opened
     * @throws com.liquilabs.vankoo.investment.domain.exceptions.WalletUnavailableException
     *         when Finance cannot be reached or answers something unexpected
     * @throws IllegalStateException when the key was already used for a different amount
     */
    void debitForInvestment(UserId investorId, Money amount, String idempotencyKey);
}
