package uk.gov.ida.verifymatchingservicetesttool.scenarios;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.ida.verifymatchingservicetesttool.checkers.AccountCreationChecker;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.FileUtilsResolver;
import uk.gov.ida.verifymatchingservicetesttool.utils.FileUtils;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

@ExtendWith(ApplicationConfigurationResolver.class)
@ExtendWith(FileUtilsResolver.class)
@ExtendWith(AccountCreationChecker.class)
public class UserAccountCreationScenario extends ScenarioBase {

    public UserAccountCreationScenario(ApplicationConfiguration configuration, FileUtils fileUtils) {
        super(configuration, fileUtils);
    }

    @Test
    @DisplayName("Simple user account creation request")
    public void runForUserAccountCreation() throws Exception {
        Response response = client.target(configuration.getLocalMatchingServiceAccountCreationUrl())
            .request(APPLICATION_JSON)
            .post(Entity.json(fileUtils.readFromResources("user-account-creation.json")));

        assertSuccessFailure(response);
    }
}
