package uk.gov.ida.verifymatchingservicetesttool.scenarios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@ExtendWith(ApplicationConfigurationResolver.class)
public class LevelOfAssuranceOneScenario extends ScenarioBase {

    public LevelOfAssuranceOneScenario(ApplicationConfiguration configuration) {
        super(configuration);
    }

    @Test
    @DisplayName("Simple request with level of assurance 1")
    public void runForSimpleCase() {
        Response response = client.target(configuration.getLocalMatchingServiceMatchUrl())
            .request(APPLICATION_JSON)
            .post(Entity.json(fileUtils.readFromResources("LoA1-simple-case.json")));

        validateMatchNoMatch(response);
    }

    @Test
    @DisplayName("Complex request with level of assurance 1")
    public void runForComplexCase() {
        String jsonString = fileUtils.readFromResources("LoA1-extensive-case.json")
            .replace("%yesterdayDate%", Instant.now().minus(1, DAYS).toString())
            .replace("%within405days-100days%", Instant.now().minus(100, DAYS).toString())
            .replace("%within405days-101days%", Instant.now().minus(150, DAYS).toString())
            .replace("%within405days-200days%", Instant.now().minus(400, DAYS).toString());

        Response response = client.target(configuration.getLocalMatchingServiceMatchUrl())
            .request(APPLICATION_JSON)
            .post(Entity.json(jsonString));

        validateMatchNoMatch(response);
    }
}
