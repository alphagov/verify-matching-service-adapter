package uk.gov.ida.verifymatchingservicetesttool.tests;

import org.junit.Test;
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
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;

public class OptionalAddressFieldsExcludedScenario {

    private FileUtils fileUtils = new FileUtils();
    private Client client = ClientBuilder.newClient();

    @Test
    public void runCase() throws IOException {
        String jsonString = fileUtils.readFromResources("simple-case-excluding-optional-address-fields.json");
        Response response = client.target("http://localhost:50130/local-matching/match")
            .request("application/json")
            .post(Entity.json(jsonString));

        Map<String, String> result = response.readEntity(new GenericType<Map<String, String>>() {{ }});

        assertThat(result.keySet(), is(new HashSet<String>() {{ add("result"); }}));
        assertThat(result.get("result"), anyOf(is("match"), is("no-match")));
    }
}
