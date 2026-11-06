package com.liquilabs.vankoo.investment.interfaces.rest;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.cloud.stream.default-binder=test")
@AutoConfigureMockMvc
@Import(TestChannelBinderConfiguration.class)
@ActiveProfiles("test")
class AuctionGreenCertificationIntegrationTest {

    private static final String GREEN_EVALUATION = """
            {
              "assessmentId": "risk-green-1",
              "riskGrade": "B",
              "fullBalanceOutstanding": true,
              "assessedAt": "2026-10-16T17:00:00Z",
              "greenCertification": {
                "certificationId": "green-cert-1",
                "standard": "ISO_14001",
                "issuer": "SGS del Perú S.A.C.",
                "validUntil": "2028-03-31",
                "criteria": [
                  {"code": "IN_FORCE", "detail": "Vigente hasta el 2028-03-31"},
                  {"code": "ACCEPTED_STANDARD", "detail": "ISO 14001:2015"}
                ]
              }
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anEvaluationWithACertificateMakesTheAuctionGreenAndTheDetailShowsWhy() throws Exception {
        String auctionId = createAuction("invoice-green-1", false);

        mockMvc.perform(put("/api/v1/internal/auctions/{auctionId}/evaluation", auctionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(GREEN_EVALUATION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("DRAFT"))
                .andExpect(jsonPath("$.greenCertified").value(true))
                .andExpect(jsonPath("$.greenCertification.standard").value("ISO_14001"));

        mockMvc.perform(put("/api/v1/internal/auctions/{auctionId}/evaluation", auctionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(GREEN_EVALUATION))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/auctions/{auctionId}", auctionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.greenCertified").value(true))
                .andExpect(jsonPath("$.greenCertification.certificationId").value("green-cert-1"))
                .andExpect(jsonPath("$.greenCertification.issuer").value("SGS del Perú S.A.C."))
                .andExpect(jsonPath("$.greenCertification.validUntil").value("2028-03-31"))
                .andExpect(jsonPath("$.greenCertification.criteria[0].code").value("ACCEPTED_STANDARD"))
                .andExpect(jsonPath("$.greenCertification.criteria[1].code").value("IN_FORCE"))
                .andExpect(jsonPath("$.greenCertification.criteria[1].detail").value("Vigente hasta el 2028-03-31"));
    }

    @Test
    void anEvaluationWithoutACertificateLeavesTheAuctionNotGreen() throws Exception {
        String auctionId = createAuction("invoice-green-2", true);

        mockMvc.perform(put("/api/v1/internal/auctions/{auctionId}/evaluation", auctionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assessmentId": "risk-green-2",
                                  "riskGrade": "A",
                                  "fullBalanceOutstanding": true,
                                  "assessedAt": "2026-10-16T17:00:00Z"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.greenCertified").value(false))
                .andExpect(jsonPath("$.greenCertification").value(nullValue()));
    }

    @Test
    void aCertificateWithoutCriteriaOrWithAnUnknownStandardIsInvalid() throws Exception {
        String auctionId = createAuction("invoice-green-3", false);

        mockMvc.perform(put("/api/v1/internal/auctions/{auctionId}/evaluation", auctionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(GREEN_EVALUATION.replaceAll("(?s)\"criteria\": \\[.*?]", "\"criteria\": []")))
                .andExpect(status().isBadRequest());

        mockMvc.perform(put("/api/v1/internal/auctions/{auctionId}/evaluation", auctionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(GREEN_EVALUATION.replace("ISO_14001", "ISO_9001")))
                .andExpect(status().isBadRequest());
    }

    private String createAuction(String invoiceId, boolean greenCertified) throws Exception {
        String body = mockMvc.perform(post("/api/v1/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invoiceId": "%s",
                                  "mypeId": "mype-green",
                                  "invoiceAmount": 10000.00,
                                  "currency": "PEN",
                                  "greenCertified": %s,
                                  "payerRuc": "20123456789",
                                  "payerName": "Pagador S.A.",
                                  "dueDate": "2027-01-31"
                                }
                                """.formatted(invoiceId, greenCertified)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        return body.replace("\"", "");
    }
}
