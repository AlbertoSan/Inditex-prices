package com.priceservice.app.stepdefs;

import com.priceservice.app.PriceServiceApplication;
import com.priceservice.app.application.port.in.GetPriceQuery;
import com.priceservice.app.application.port.in.GetPriceUseCase;
import com.priceservice.app.domain.model.Price;
import com.priceservice.app.infrastructure.adapter.out.persistence.PriceJpaEntity;
import com.priceservice.app.infrastructure.adapter.out.persistence.PriceJpaRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ContextConfiguration;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.time.format.DateTimeParseException;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;


@SpringBootTest(classes = PriceServiceApplication.class)
@ContextConfiguration(classes = {PriceServiceApplication.class})
@CucumberContextConfiguration
public class GetProductPriceStepDefs {

    private final Logger log = LoggerFactory.getLogger(GetProductPriceStepDefs.class);

    @Autowired
    private PriceJpaRepository priceRepository;

    @Autowired
    private GetPriceUseCase getPriceUseCase;

    private Price price;
    private String formattedPrice;
    private Exception lastException = null;


    @Given("the system has price configurations set")
    public void theSystemHasPriceConfigurationsSet() {
        initializeInMemoryDBWithPriceRates();
    }

    private void initializeInMemoryDBWithPriceRates() {
        log.info("Call to initializeInMemoryDBWithPriceRates");
        priceRepository.deleteAll();

        priceRepository.save(rate("2020-06-14T00:00:00Z", "2020-12-31T23:59:59Z", 1, 0, 35.50));
        priceRepository.save(rate("2020-06-14T15:00:00Z", "2020-06-14T18:30:00Z", 2, 1, 25.45));
        priceRepository.save(rate("2020-06-15T00:00:00Z", "2020-06-15T11:00:00Z", 3, 1, 30.50));
        priceRepository.save(rate("2020-06-15T16:00:00Z", "2020-12-31T23:59:59Z", 4, 1, 38.95));
    }

    private PriceJpaEntity rate(String start, String end, int list, int priority, double amount) {
        return PriceJpaEntity.builder()
                .brandId(1)
                .productId(35455)
                .startDate(OffsetDateTime.parse(start))
                .endDate(OffsetDateTime.parse(end))
                .priceList(list)
                .priority(priority)
                .price(BigDecimal.valueOf(amount))
                .curr("EUR")
                .build();
    }

    @When("I request the price for product {string} from brand {string} at {string}")
    public void iRequestThePriceForProductFromBrandAt(String productId, String brandId, String dateTime) {
        try {
            if (isNullOrEmpty(productId) || isNullOrEmpty(brandId) || isNullOrEmpty(dateTime)) {
                throw new Exception("Missing parameter");
            }

            OffsetDateTime offsetDateTime = OffsetDateTime.parse(dateTime);
            price = getPriceUseCase.getPrice(
                    new GetPriceQuery(Integer.parseInt(productId), Integer.parseInt(brandId), offsetDateTime));

            if (price == null) {
                throw new Exception("Price not found");
            }

            formattedPrice = String.format(Locale.US, "%.2f", price.getAmount());
            lastException = null;
        } catch (DateTimeParseException _) {
            lastException = new Exception("Invalid date format");
        } catch (Exception e) {
            lastException = e;
        }
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    @Then("the system should return a price of {string} EUR")
    public void theSystemShouldReturnAPriceOfEUR(String expectedPrice) {
        assertEquals(expectedPrice, formattedPrice);
    }

    @Then("the system should return a {string} error")
    public void theSystemShouldReturnAError(String expectedMessage) {
        assertNotNull(lastException, "Expected an exception to be thrown");
        assertEquals(expectedMessage, lastException.getMessage());
    }
}
