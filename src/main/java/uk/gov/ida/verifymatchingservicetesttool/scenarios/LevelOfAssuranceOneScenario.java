package uk.gov.ida.verifymatchingservicetesttool.scenarios;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import uk.gov.ida.verifymatchingservicetesttool.configurations.ApplicationConfiguration;
import uk.gov.ida.verifymatchingservicetesttool.resolvers.ApplicationConfigurationResolver;
import uk.gov.ida.verifymatchingservicetesttool.utils.FileUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.io.IOException;
import java.time.Instant;
import java.util.HashSet;
import java.util.Map;

import static java.time.temporal.ChronoUnit.DAYS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;

@ExtendWith(ApplicationConfigurationResolver.class)
public class LevelOfAssuranceOneScenario {

    private FileUtils fileUtils = new FileUtils();
    private Client client = ClientBuilder.newClient();
    private ApplicationConfiguration configuration;

    public LevelOfAssuranceOneScenario(ApplicationConfiguration configuration) {
        this.configuration = configuration;
    }

    @Test
    public void runForSimpleCase() throws IOException {
        String jsonString = fileUtils.readFromResources("LoA1-simple-case.json");
        Response response = client.target(configuration.getLocalMatchingServiceMatchUrl())
            .request(APPLICATION_JSON)
            .post(Entity.json(jsonString));

        Map<String, String> result = response.readEntity(new GenericType<Map<String, String>>() {{ }});

        assertThat(result.keySet(), is(new HashSet<String>() {{ add("result"); }}));
        assertThat(result.get("result"), anyOf(is("match"), is("no-match")));
    }

    @Test
    public void runForExtensiveCaseRRR() throws IOException {
        String jsonString = fileUtils.readFromResources("LoA1-extensive-case.json")
            .replace("%yesterdayDate%", Instant.now().minus(1, DAYS).toString())
            .replace("%within405days-100days%", Instant.now().minus(405-100, DAYS).toString())
            .replace("%within405days-101days%", Instant.now().minus(405-101, DAYS).toString())
            .replace("%within405days-200days%", Instant.now().minus(405-200, DAYS).toString());

        Response response = client.target(configuration.getLocalMatchingServiceMatchUrl())
            .request(APPLICATION_JSON)
            .post(Entity.json(jsonString));

        Map<String, String> result = response.readEntity(new GenericType<Map<String, String>>() {{ }});

        assertThat(result.keySet(), is(new HashSet<String>() {{ add("result"); }}));
        assertThat(result.get("result"), anyOf(is("match"), is("no-match")));
    }
}
