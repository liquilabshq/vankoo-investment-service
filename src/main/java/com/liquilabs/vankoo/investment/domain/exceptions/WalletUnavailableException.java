package com.liquilabs.vankoo.investment.domain.exceptions;

/**
 * Finance could not be asked to debit, or answered in a way Investment cannot
 * read. Nothing was granted; whether the debit happened is unknown, which is
 * why a retry must reuse the same {@code transactionId}: Finance replays the
 * debit for the same key instead of charging again.
 */
public class WalletUnavailableException extends RuntimeException {

    public static final String CODE = "wallet-unavailable";

    public WalletUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }

    public WalletUnavailableException(String message) {
        super(message);
    }
}
