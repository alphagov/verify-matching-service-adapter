package uk.gov.ida.verifymatchingservicetesttool.scenarios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.FileUtilsResolver;
import uk.gov.ida.verifymatchingservicetesttool.utils.FileUtils;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import java.time.Instant;

import static java.time.temporal.ChronoUnit.DAYS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@ExtendWith(ApplicationConfigurationResolver.class)
@ExtendWith(FileUtilsResolver.class)
public class SpecialNameCaseScenario extends ScenarioBase {

    public SpecialNameCaseScenario(ApplicationConfiguration configuration, FileUtils fileUtils) {
        super(configuration, fileUtils);
    }

    @Test
    @DisplayName("Simple request with special characters in names")
    public void runForSimpleCase() throws Exception {
        Response response = client.target(configuration.getLocalMatchingServiceMatchUrl())
            .request(APPLICATION_JSON)
            .post(Entity.json(fileUtils.readFromResources("special-name-case.json")));

        validateMatchNoMatch(response);
    }
}
