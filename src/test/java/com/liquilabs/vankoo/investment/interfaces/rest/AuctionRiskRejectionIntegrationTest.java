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

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.cloud.stream.default-binder=test")
@AutoConfigureMockMvc
@Import(TestChannelBinderConfiguration.class)
@ActiveProfiles("test")
class AuctionRiskRejectionIntegrationTest {

    private static final String REJECTION = """
            {
              "assessmentId": "risk-rejection-1",
              "reasonCode": "PAYER_DOMICILE_NOT_FOUND",
              "reason": "El pagador figura como NO HABIDO en SUNAT",
              "assessedAt": "2026-09-07T17:00:00Z"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @Test
    void rejectsAnAuctionAwaitingItsRiskAssessmentAndShowsTheReason() throws Exception {
        String auctionId = createAuction("invoice-rejection-1", "mype-rejection-1");

        mockMvc.perform(post("/api/v1/internal/auctions/{auctionId}/rejection", auctionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REJECTION))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.assessmentId").value("risk-rejection-1"))
                .andExpect(jsonPath("$.rejectionReasonCode").value("PAYER_DOMICILE_NOT_FOUND"))
                .andExpect(jsonPath("$.rejectionReason").value("El pagador figura como NO HABIDO en SUNAT"))
                .andExpect(jsonPath("$.rejectedAt").exists());

        mockMvc.perform(post("/api/v1/internal/auctions/{auctionId}/rejection", auctionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REJECTION))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/auctions/{auctionId}", auctionId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("REJECTED"))
                .andExpect(jsonPath("$.rejectionReasonCode").value("PAYER_DOMICILE_NOT_FOUND"));
    }

    @Test
    void anAuctionThatWasAlreadyEvaluatedCannotBeRejected() throws Exception {
        String auctionId = createAuction("invoice-rejection-2", "mype-rejection-2");
        mockMvc.perform(put("/api/v1/internal/auctions/{auctionId}/evaluation", auctionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assessmentId": "risk-evaluation-2",
                                  "riskGrade": "A",
                                  "fullBalanceOutstanding": true,
                                  "assessedAt": "2026-09-07T17:00:00Z"
                                }
                                """))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/v1/internal/auctions/{auctionId}/rejection", auctionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REJECTION))
                .andExpect(status().isConflict());
    }

    @Test
    void anUnknownAuctionIsNotFound() throws Exception {
        mockMvc.perform(post("/api/v1/internal/auctions/{auctionId}/rejection", "missing-auction")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(REJECTION))
                .andExpect(status().isNotFound());
    }

    @Test
    void aRejectionWithoutReasonIsInvalid() throws Exception {
        String auctionId = createAuction("invoice-rejection-3", "mype-rejection-3");

        mockMvc.perform(post("/api/v1/internal/auctions/{auctionId}/rejection", auctionId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "assessmentId": "risk-rejection-3",
                                  "reasonCode": "PAYER_NOT_ACTIVE",
                                  "reason": " ",
                                  "assessedAt": "2026-09-07T17:00:00Z"
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.reason").exists());
    }

    private String createAuction(String invoiceId, String mypeId) throws Exception {
        return mockMvc.perform(post("/api/v1/auctions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "invoiceId": "%s",
                                  "mypeId": "%s",
                                  "invoiceAmount": 10000.00,
                                  "currency": "PEN",
                                  "greenCertified": false,
                                  "payerRuc": "20123456789",
                                  "payerName": "Pagador S.A.",
                                  "dueDate": "2027-01-31"
                                }
                                """.formatted(invoiceId, mypeId)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
    }
}
