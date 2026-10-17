package com.liquilabs.vankoo.investment.domain.model.valueobjects;

/**
 * The environmental standards whose certificate makes an auction green.
 *
 * The risk service decides which certificate qualifies; this list only says which
 * standards Investment knows how to show.
 */
public enum GreenStandard {
    ISO_14001,
    ISO_50001
}
