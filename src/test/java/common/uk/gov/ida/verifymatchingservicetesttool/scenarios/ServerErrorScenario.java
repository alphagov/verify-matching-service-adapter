package common.uk.gov.ida.verifymatchingservicetesttool.scenarios;

import org.junit.jupiter.api.Test;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.scenarios.ScenarioBase;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.INTERNAL_SERVER_ERROR;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class ServerErrorScenario extends ScenarioBase {

    public ServerErrorScenario(ApplicationConfiguration configuration) {
        super(configuration);
    }

    @Test
    public void runForInternalServerErrorCase() {
        Response response = client.target(configuration.getLocalMatchingServiceMatchUrl())
            .request(APPLICATION_JSON)
            .post(Entity.json(fileUtils.readFromResources("error-simple-case.json")));

        assertThat(response.getStatus(), is(INTERNAL_SERVER_ERROR.getStatusCode()));
    }

}
