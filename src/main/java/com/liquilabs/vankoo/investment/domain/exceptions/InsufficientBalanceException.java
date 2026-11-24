package com.liquilabs.vankoo.investment.domain.exceptions;

/** The investor's wallet does not cover the investment. No money moved and no share was granted. */
public class InsufficientBalanceException extends RuntimeException {

    public static final String CODE = "insufficient-balance";

    public InsufficientBalanceException(String message) {
        super(message);
    }
}
