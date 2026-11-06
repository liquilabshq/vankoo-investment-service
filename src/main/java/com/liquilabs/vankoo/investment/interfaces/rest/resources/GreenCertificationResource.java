package com.liquilabs.vankoo.investment.interfaces.rest.resources;

import com.liquilabs.vankoo.investment.domain.model.valueobjects.GreenCertification;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.GreenEvidence;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.GreenStandard;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.util.List;

/**
 * The certificate that makes an auction green and the criteria it passed, as the risk
 * service sends it and as the auction detail shows it.
 */
public record GreenCertificationResource(
        @NotBlank @Size(max = 100) String certificationId,
        @NotNull GreenStandard standard,
        @NotBlank @Size(max = 200) String issuer,
        @NotNull LocalDate validUntil,
        @NotEmpty @Size(max = 20) List<@Valid @NotNull GreenCriterionResource> criteria
) {
    public static GreenCertificationResource from(GreenEvidence green) {
        if (green == null) {
            return null;
        }
        GreenCertification certification = green.certification();
        return new GreenCertificationResource(
                certification.certificationId(),
                certification.standard(),
                certification.issuer(),
                certification.validUntil(),
                green.criteria().stream().map(GreenCriterionResource::from).toList()
        );
    }

    public GreenEvidence toValueObject() {
        return new GreenEvidence(
                new GreenCertification(certificationId, standard, issuer, validUntil),
                criteria.stream().map(GreenCriterionResource::toValueObject).toList()
        );
    }
}
