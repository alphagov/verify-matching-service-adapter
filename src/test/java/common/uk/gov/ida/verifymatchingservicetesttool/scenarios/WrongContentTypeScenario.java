package common.uk.gov.ida.verifymatchingservicetesttool.scenarios;

import org.junit.jupiter.api.Test;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.scenarios.ScenarioBase;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;

import static javax.ws.rs.core.MediaType.APPLICATION_XHTML_XML_TYPE;
import static javax.ws.rs.core.Response.Status.UNSUPPORTED_MEDIA_TYPE;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class WrongContentTypeScenario extends ScenarioBase {

    public WrongContentTypeScenario(ApplicationConfiguration configuration) {
        super(configuration);
    }

    @Test
    public void runForNonJsonContentTypeCase() {
        Response response = client.target(configuration.getLocalMatchingServiceMatchUrl())
            .request(APPLICATION_XHTML_XML_TYPE)
            .post(Entity.json(fileUtils.readFromResources("wrong-content-type-case.json")));

        assertThat(response.getStatus(), is(UNSUPPORTED_MEDIA_TYPE.getStatusCode()));
    }
}
