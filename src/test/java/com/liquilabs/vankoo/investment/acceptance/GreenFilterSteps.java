package com.liquilabs.vankoo.investment.acceptance;

import com.jayway.jsonpath.JsonPath;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.AuctionStatus;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.Currency;
import com.liquilabs.vankoo.investment.domain.model.valueobjects.ScoreGrade;
import com.liquilabs.vankoo.investment.infrastructure.configuration.MarketplaceCacheConfiguration;
import com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.views.AuctionMarketplaceViewEntity;
import com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.views.AuctionMarketplaceViewRepository;
import com.liquilabs.vankoo.investment.infrastructure.persistence.jpa.views.MarketplaceProcessedEventRepository;
import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.CacheManager;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Steps de US11 (filtro de impacto positivo) contra el endpoint real del marketplace,
 * con la proyección que lee el inversionista en H2.
 *
 * <p>El distintivo verde solo existe si Risk validó un certificado vigente al evaluar la
 * factura (ver {@code Auction.evaluate}), así que «verde vigente» es una operación verde que
 * sigue activa. El mensaje para el usuario lo muestra la app móvil cuando la lista llega vacía.
 */
public class GreenFilterSteps {

    private static final Instant NOW = Instant.parse("2026-11-24T15:00:00Z");

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AuctionMarketplaceViewRepository viewRepository;

    @Autowired
    private MarketplaceProcessedEventRepository processedEventRepository;

    @Autowired
    private CacheManager cacheManager;

    private String response;

    @Given("que el marketplace contiene estas operaciones:")
    public void marketplaceWith(DataTable table) {
        processedEventRepository.deleteAll();
        viewRepository.deleteAll();
        cacheManager.getCache(MarketplaceCacheConfiguration.MARKETPLACE_CACHE).clear();

        for (Map<String, String> row : table.asMaps()) {
            viewRepository.save(view(auctionId(row.get("operación")), auctionStatus(row.get("estado")),
                    "sí".equals(row.get("distintivo verde"))));
        }
    }

    @When("^el inversionista (?:activa|aplica) el filtro de impacto positivo$")
    public void appliesGreenFilter() throws Exception {
        response = mockMvc.perform(get("/api/v1/auctions/marketplace").param("greenCertified", "true"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString(StandardCharsets.UTF_8);
    }

    @Then("el sistema devuelve únicamente las operaciones con certificación verde vigente:")
    public void returnsOnlyGreen(DataTable expected) {
        List<String> expectedIds = expected.asMaps().stream().map(row -> auctionId(row.get("operación"))).toList();
        List<String> ids = JsonPath.read(response, "$.content[*].auctionId");
        List<Boolean> green = JsonPath.read(response, "$.content[*].greenCertified");

        assertThat(ids).containsExactlyInAnyOrderElementsOf(expectedIds);
        assertThat(green).containsOnly(true);
    }

    @Then("el sistema devuelve una colección vacía")
    public void returnsEmptyCollection() {
        List<String> ids = JsonPath.read(response, "$.content[*].auctionId");
        assertThat(ids).isEmpty();
    }

    @Then("informa que no hay oportunidades sostenibles disponibles")
    public void reportsNoGreenOpportunities() {
        Integer total = JsonPath.read(response, "$.totalElements");
        assertThat(total).isZero();
    }

    private static String auctionId(String name) {
        return UUID.nameUUIDFromBytes(("us11-" + name).getBytes(StandardCharsets.UTF_8)).toString();
    }

    private static AuctionStatus auctionStatus(String name) {
        return switch (name) {
            case "publicada" -> AuctionStatus.PUBLISHED;
            case "en fondeo" -> AuctionStatus.FUNDING;
            case "vencida" -> AuctionStatus.EXPIRED;
            default -> throw new IllegalArgumentException("Unknown status: " + name);
        };
    }

    private static AuctionMarketplaceViewEntity view(String auctionId, AuctionStatus status, boolean green) {
        String eventId = UUID.randomUUID().toString();
        BigDecimal target = new BigDecimal("9000.00");
        AuctionMarketplaceViewEntity view = AuctionMarketplaceViewEntity.preliminary(
                auctionId, "invoice-" + auctionId.substring(0, 8), "mype-us11", "20123456789", "Pagador S.A.",
                LocalDate.of(2026, 12, 31), new BigDecimal("10000.00"), Currency.PEN,
                AuctionStatus.PENDING_VERIFICATION_RISK, green, 1, eventId, NOW);
        view.publish(
                view.getInvoiceId(), view.getMypeId(), view.getPayerRuc(), view.getPayerName(), view.getDueDate(),
                view.getInvoiceAmount(), new BigDecimal("9250.00"), target, BigDecimal.ZERO.setScale(2),
                Currency.PEN, new BigDecimal("0.15"), new BigDecimal("0.04"), 90, ScoreGrade.B,
                AuctionStatus.PUBLISHED, green, NOW, NOW.plusSeconds(86_400), 1, eventId, NOW);
        if (status == AuctionStatus.FUNDING) {
            view.addPartition(new BigDecimal("500.00"), 1, eventId, NOW);
        } else if (status == AuctionStatus.EXPIRED) {
            view.expire(1, eventId, NOW);
        }
        return view;
    }
}
