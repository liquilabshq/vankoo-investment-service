package com.liquilabs.vankoo.investment.domain.model.valueobjects;

import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;

/**
 * Why an auction is green: the certificate and the criteria it passed.
 *
 * US10 asks for the green badge to keep the criteria behind it, so a certificate never
 * arrives without at least one.
 */
public record GreenEvidence(GreenCertification certification, List<GreenCriterion> criteria) {
    public GreenEvidence {
        Objects.requireNonNull(certification, "Green certification is required");
        if (criteria == null || criteria.isEmpty()) {
            throw new IllegalArgumentException("A green certification needs the criteria it passed");
        }
        // Sorted by code, the order they are stored in, so equal evidence compares equal.
        criteria = criteria.stream().sorted(Comparator.comparing(GreenCriterion::code)).toList();
        if (new HashSet<>(criteria.stream().map(GreenCriterion::code).toList()).size() != criteria.size()) {
            throw new IllegalArgumentException("Green criteria cannot repeat a code");
        }
    }
}
