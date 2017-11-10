package uk.gov.ida.verifymatchingservicetesttool.scenarios;

import org.hamcrest.core.Is;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ConfigurationReader;
import uk.gov.ida.verifymatchingservicetesttool.utils.FileUtils;

import javax.validation.constraints.AssertFalse;
import javax.validation.constraints.AssertTrue;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.net.URI;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.CoreMatchers.anyOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.junit.jupiter.api.Assumptions.assumingThat;


public class UserAccountCreationScenario {
    private static URI accountCreationUrl = ConfigurationReader.getConfiguration().getLocalMatchingServiceAccountCreationUrl();
    private FileUtils fileUtils = new FileUtils();

    @Test
    public void runForUserAccountCreation() {
        assumingThat(accountCreationUrl != null, () -> {
            Response response = ClientBuilder.newClient()
                    .target(accountCreationUrl)
                    .request("application/json")
                    .post(Entity.json(fileUtils.readFromResources("user-account-creation.json")));

            Map<String, String> result = response.readEntity(new GenericType<Map<String, String>>() {{ }});

            assertThat(result.keySet(), Is.is(new HashSet<String>() {{ add("result"); }}));
            assertThat(result.get("result"), anyOf(is("success"), is("failure")));
        });
    }
}
