package simulations;

import io.gatling.javaapi.core.ScenarioBuilder;
import io.gatling.javaapi.core.Simulation;
import io.gatling.javaapi.http.HttpProtocolBuilder;

import java.time.Duration;

import static io.gatling.javaapi.core.CoreDsl.global;
import static io.gatling.javaapi.core.CoreDsl.rampUsers;
import static io.gatling.javaapi.core.CoreDsl.scenario;
import static io.gatling.javaapi.http.HttpDsl.http;
import static io.gatling.javaapi.http.HttpDsl.status;

/**
 * Prueba de carga sobre GET /prices con los 5 escenarios conocidos de la prueba.
 *
 * IMPORTANTE: el rate limiter (5 req/s por IP) estrangularía la carga. Arranca
 * la aplicación con -Dratelimit.enabled=false antes de lanzar la simulación:
 *
 *   1) java -jar target/ms-price-service.jar --ratelimit.enabled=false
 *   2) mvn -Pgatling gatling:test
 *
 * La URL base se puede sobreescribir con -DbaseUrl=http://host:puerto
 */
public class PriceSimulation extends Simulation {

    private final HttpProtocolBuilder httpProtocol = http
            .baseUrl(System.getProperty("baseUrl", "http://localhost:8080"))
            .acceptHeader("application/json");

    private final String pricePath =
            System.getProperty("pricePath", "/v1/brands/1/products/35455/price");

    private final ScenarioBuilder scn = scenario("Get applicable price")
            .exec(http("14th 10:00 -> 35.50")
                    .get(pricePath).queryParam("date", "2020-06-14T10:00:00Z")
                    .check(status().is(200)))
            .exec(http("14th 16:00 -> 25.45")
                    .get(pricePath).queryParam("date", "2020-06-14T16:00:00Z")
                    .check(status().is(200)))
            .exec(http("14th 21:00 -> 35.50")
                    .get(pricePath).queryParam("date", "2020-06-14T21:00:00Z")
                    .check(status().is(200)))
            .exec(http("15th 10:00 -> 30.50")
                    .get(pricePath).queryParam("date", "2020-06-15T10:00:00Z")
                    .check(status().is(200)))
            .exec(http("16th 21:00 -> 38.95")
                    .get(pricePath).queryParam("date", "2020-06-16T21:00:00Z")
                    .check(status().is(200)));

    public PriceSimulation() {
        setUp(
                scn.injectOpen(rampUsers(200).during(Duration.ofSeconds(30)))
        ).protocols(httpProtocol)
                .assertions(
                        global().responseTime().percentile(95.0).lt(800),
                        global().successfulRequests().percent().gt(99.0)
                );
    }
}
