package common.uk.gov.ida.verifymatchingservicetesttool.services;

import com.github.tomakehurst.wiremock.WireMockServer;
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response.Status;
import java.net.URI;

import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.containing;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static common.uk.gov.ida.verifymatchingservicetesttool.services.LocalMatchingServiceStub.MatchingResult.MATCH;
import static common.uk.gov.ida.verifymatchingservicetesttool.services.LocalMatchingServiceStub.MatchingResult.NO_MATCH;
import static common.uk.gov.ida.verifymatchingservicetesttool.services.LocalMatchingServiceStub.MatchingResult.SUCCESS;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static javax.ws.rs.core.Response.Status.OK;

public class LocalMatchingServiceStub {

    public enum MatchingResult {

        MATCH("match"), NO_MATCH("no-match"), SUCCESS("success");

        private String result;

        MatchingResult(String result) {
            this.result = result;
        }

        public String getResult() {
            return result;
        }
    }

    public static final String RELATIVE_MATCH_URL = "/local-matching/match";
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

    public void ensureDefaultMatchScenariosExist() {
        server.stubFor(post(urlEqualTo(RELATIVE_MATCH_URL))
            .withRequestBody(containing("default-match-id"))
            .willReturn(getResponseBuilderFor(MATCH))
        );

        server.stubFor(post(urlEqualTo(RELATIVE_MATCH_URL))
            .withRequestBody(containing("default-no-match-id"))
            .willReturn(getResponseBuilderFor(NO_MATCH))
        );

        server.stubFor(post(urlEqualTo(RELATIVE_ACCOUNT_CREATION_URL))
            .willReturn(getResponseBuilderFor(SUCCESS))
        );
    }

    public void ensureResponseHeaderFor(String relativeUrl, MediaType mediaType) {
        server.stubFor(post(urlEqualTo(relativeUrl))
            .willReturn(
                aResponse()
                    .withStatus(OK.getStatusCode())
                    .withHeader("Content-Type", mediaType.toString())
            )
        );
    }

    public void ensureResponseFor(String relativeUrl, Status status, String body){
        server.stubFor(post(urlEqualTo(relativeUrl))
            .willReturn(aResponse()
                .withStatus(status.getStatusCode())
                .withHeader("Content-Type", APPLICATION_JSON)
                .withBody(body)
            )
        );
    }

    public URI getMatchingUrl() {
        return URI.create(String.format(MATCH_URL_PATTERN, server.port()));
    }

    public URI getAccountCreationUrl() {
        return URI.create(String.format(ACCOUNT_CREATION_URL_PATTERN, server.port()));
    }

    private ResponseDefinitionBuilder getResponseBuilderFor(MatchingResult matchingResult) {
        return aResponse()
            .withStatus(OK.getStatusCode())
            .withHeader("Content-Type", APPLICATION_JSON)
            .withBody(String.format("{\"result\": \"%s\"}", matchingResult.getResult()));
    }
}
