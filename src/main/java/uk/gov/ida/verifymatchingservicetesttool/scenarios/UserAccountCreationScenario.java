package uk.gov.ida.verifymatchingservicetesttool.scenarios;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Map;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

@ExtendWith(ApplicationConfigurationResolver.class)
public class UserAccountCreationScenario extends ScenarioBase {

    public UserAccountCreationScenario(ApplicationConfiguration configuration) {
        super(configuration);
    }

    @Test
    public void runForUserAccountCreation() {
        assumeTrue(
            configuration.getLocalMatchingServiceAccountCreationUrl() != null,
            "Test aborted as no user account creation is not configured."
        );

        Response response = client.target(configuration.getLocalMatchingServiceAccountCreationUrl())
            .request(APPLICATION_JSON)
            .post(Entity.json(fileUtils.readFromResources("user-account-creation.json")));

        Map<String, String> result = response.readEntity(new GenericType<Map<String, String>>() {{ }});

        assertThat(result.keySet(), is(new HashSet<String>() {{ add("result"); }}));
        assertThat(result.get("result"), anyOf(is("success"), is("failure")));
    }
}
