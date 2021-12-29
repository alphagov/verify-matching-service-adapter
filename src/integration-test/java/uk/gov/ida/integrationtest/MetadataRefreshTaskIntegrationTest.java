package uk.gov.ida.integrationtest;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.RegisterExtension;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppExtension;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import static org.assertj.core.api.Assertions.assertThat;

public class MetadataRefreshTaskIntegrationTest {

    @RegisterExtension
    static MatchingServiceAdapterAppExtension EXT = new MatchingServiceAdapterAppExtension(true);

    @Test
    public void verifyFederationMetadataRefreshTaskWorks() {
        Client client = EXT.client();
        final Response response = client.target(UriBuilder.fromUri("http://localhost")
                .path("/tasks/metadata-refresh")
                .port(EXT.getAdminPort())
                .build())
                .request()
                .post(Entity.text("refresh!"));
        assertThat(response.getStatus()).isEqualTo(Response.Status.OK.getStatusCode());
    }

}
