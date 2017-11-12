package common.uk.gov.ida.verifymatchingservicetesttool.services;

import com.github.tomakehurst.wiremock.WireMockServer;

import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;

public class LocalMatchingServiceStub {

    private static final String RELATIVE_MATCH_URL = "/local-matching/match";
    private static final String RELATIVE_ACCOUNT_CREATION_URL = "/local-matching/create-user";
    private final static String BASE_URL_PATTERN = "http://localhost:%d";
    private final static String MATCH_URL_PATTERN = BASE_URL_PATTERN + RELATIVE_MATCH_URL;
    private final static String ACCOUNT_CREATION_URL_PATTERN = BASE_URL_PATTERN + RELATIVE_ACCOUNT_CREATION_URL;

    private static WireMockServer server = new WireMockServer(wireMockConfig().dynamicPort());

    public void start() {
        server.start();
    }

    public void stop() {
        server.stop();
    }

    public void ensureDefaultMatchScenariosExists() {
        server.stubFor(
            post(urlEqualTo(RELATIVE_MATCH_URL))
                .withRequestBody(containing("default-match-id"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withBody("{\"result\": \"match\"}")
                )
        );

        server.stubFor(
            post(urlEqualTo(RELATIVE_MATCH_URL))
                .withRequestBody(containing("default-no-match-id"))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withBody("{\"result\": \"no-match\"}")
                )
        );

        server.stubFor(
            post(urlEqualTo(RELATIVE_ACCOUNT_CREATION_URL))
                .willReturn(aResponse()
                    .withStatus(200)
                    .withHeader("Content-Type", APPLICATION_JSON)
                    .withBody("{\"result\": \"success\"}")
                )
        );
    }

    public URI getMatchingUrl() {
        return URI.create(String.format(MATCH_URL_PATTERN, server.port()));
    }

    public URI getAccountCreationUrl() {
        return URI.create(String.format(ACCOUNT_CREATION_URL_PATTERN, server.port()));
    }
}
