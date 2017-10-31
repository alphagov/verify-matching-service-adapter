package uk.gov.ida.verifymatchingservicetesttool.tests;

import org.junit.Test;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.Response;
import java.util.HashSet;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.AnyOf.anyOf;
import static org.hamcrest.core.Is.is;

public class BasicMatchingTests {
    @Test
    public void shouldRespondToValidRequestsWithEitherMatchOrNoMatch() {
        Client client = ClientBuilder.newClient();

        Response response = client.target("http://localhost:50130/local-matching/match")
            .request("application/json")
            .post(Entity.json(null));

        Map<String, String> result = response.readEntity(new GenericType<Map<String, String>>() {{}});

        assertThat(result.keySet(), is(new HashSet<String>() {{ add("result"); }}));
        assertThat(result.get("result"), anyOf(is("match"), is("no-match")));
    }
}
