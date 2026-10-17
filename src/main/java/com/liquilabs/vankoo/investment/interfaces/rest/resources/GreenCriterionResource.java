package com.liquilabs.vankoo.investment.interfaces.rest.resources;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.GreenCriterion;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record GreenCriterionResource(
        @NotBlank @Size(max = 40) String code,
        @Size(max = 300) String detail
) {
    public static GreenCriterionResource from(GreenCriterion criterion) {
        return new GreenCriterionResource(criterion.code(), criterion.detail());
    }

    public GreenCriterion toValueObject() {
        return new GreenCriterion(code, detail);
    }
}
