package uk.gov.ida.verifymatchingservicetesttool.scenarios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(ApplicationConfigurationResolver.class)
public class UserAccountCreationScenario extends ScenarioBase {

    public UserAccountCreationScenario(ApplicationConfiguration configuration) {
        super(configuration);
    }

    @Test
    @DisplayName(
        "Simple user account creation request\n" +
        "(remove accountCreationUrl from verify-matching-service-test-tool.yml " +
        "to skip this if you don't need to test account creation)"
    )
    public void runForUserAccountCreation() throws Exception {
        assumeTrue(
            configuration.getLocalMatchingServiceAccountCreationUrl() != null,
            "Test aborted as no user account creation is not configured."
        );

        Response response = client.target(configuration.getLocalMatchingServiceAccountCreationUrl())
            .request(APPLICATION_JSON)
            .post(Entity.json(fileUtils.readFromResources("user-account-creation.json")));

        validateSuccessFailure(response);
    }
}
