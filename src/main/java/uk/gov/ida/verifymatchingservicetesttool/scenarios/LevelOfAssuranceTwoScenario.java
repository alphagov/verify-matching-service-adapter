package uk.gov.ida.verifymatchingservicetesttool.scenarios;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;

@ExtendWith(ApplicationConfigurationResolver.class)
public class LevelOfAssuranceTwoScenario extends ScenarioBase {

    public LevelOfAssuranceTwoScenario(ApplicationConfiguration configuration) {
        super(configuration);
    }

    @Test
    public void runForWhenAllElementsAreVerifiedAndNoMultipleValues() {
        Response response = client.target(configuration.getLocalMatchingServiceMatchUrl())
            .request(APPLICATION_JSON)
            .post(Entity.json(fileUtils.readFromResources("LoA2-simple-case.json")));

        validateMatchNoMatch(response);
    }

    @Test
    public void runForExtensiveCase() {
        String jsonString = fileUtils.readFromResources("LoA2-extensive-case.json")
            .replace("%yesterdayDate%", Instant.now().minus(1, DAYS).toString())
            .replace("%within405days-100days%", Instant.now().minus(405-100, DAYS).toString())
            .replace("%within405days-101days%", Instant.now().minus(405-101, DAYS).toString())
            .replace("%within405days-200days%", Instant.now().minus(405-200, DAYS).toString())
            .replace("%within180days-100days%", Instant.now().minus(105-100, DAYS).toString())
            .replace("%within180days-101days%", Instant.now().minus(105-101, DAYS).toString())
            .replace("%within180days-150days%", Instant.now().minus(105-150, DAYS).toString());

        Response response = client.target(configuration.getLocalMatchingServiceMatchUrl())
            .request(APPLICATION_JSON)
            .post(Entity.json(jsonString));

        validateMatchNoMatch(response);
    }
}
