package uk.gov.ida.verifymatchingservicetesttool.scenarios;

import org.hamcrest.CoreMatchers;
import org.hamcrest.core.Is;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;
import uk.gov.ida.verifymatchingservicetesttool.utils.FileUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.core.StringContains.containsString;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;

@ExtendWith(ApplicationConfigurationResolver.class)
public abstract class ScenarioBase {

    protected FileUtils fileUtils;
    protected Client client = ClientBuilder.newClient();
    protected ApplicationConfiguration configuration;

    public ScenarioBase(ApplicationConfiguration configuration, FileUtils fileUtils) {
        this.configuration = configuration;
        this.fileUtils = fileUtils;
    }

    protected Map<String, String> readEntityAsMap(Response response) {
        return response.readEntity(new GenericType<Map<String, String>>() {{ }});
    }

    protected void assertMatchNoMatch(Response response) {
        assertThat(response.getHeaderString("Content-Type"), containsString(APPLICATION_JSON));
        assertThat(response.getStatus(), is(OK.getStatusCode()));

        Map<String, String> result = readEntityAsMap(response);

        assertThat(result.keySet(), is(new HashSet<String>() {{ add("result"); }}));
        assertThat(result.get("result"), anyOf(is("match"), is("no-match")));
    }

    public void assertSuccessFailure(Response response){
        assertThat(response.getHeaderString("Content-Type"), containsString(APPLICATION_JSON));
        assertThat(response.getStatus(), Is.is(OK.getStatusCode()));

        Map<String, String> result = readEntityAsMap(response);

        assertThat(result.keySet(), is(new HashSet<String>() {{ add("result"); }}));
        assertThat(result.get("result"), anyOf(CoreMatchers.is("success"), is("failure")));
    }

    public ApplicationConfiguration getConfiguration() {
        return configuration;
    }
}
