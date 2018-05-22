package uk.gov.ida.integrationtest.interfacetests;

import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.integrationtest.helpers.RequestHelper.makeAttributeQueryRequest;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA1;

import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;

public abstract class BaseTestToolInterfaceTest {
    private final SignatureAlgorithm signatureAlgorithmForHub = new SignatureRSASHA1();
    private final DigestAlgorithm digestAlgorithmForHub = new DigestSHA256();
    protected String MATCHING_SERVICE_URI;
    protected String UNKNOWN_USER_URI;
    private static final ObjectMapper objectMapper = Jackson.newObjectMapper().setDateFormat(ISO8601DateFormat.getDateInstance());
    protected static final Integer yesterday = 1;
    protected static final Integer inRange405to100 = 100;
    protected static final Integer inRange405to101 = 150;
    protected static final Integer inRange405to200 = 400;
    protected static final Integer inRange180to100 = 100;
    protected static final Integer inRange180to101 = 140;
    protected static final Integer inRange180to150 = 160;

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(wireMockConfig().port(1234));

    protected static ConfigOverride[] configRules = new ConfigOverride[] {
        ConfigOverride.config("localMatchingService.matchUrl", "http://localhost:1234/match"),
        ConfigOverride.config("localMatchingService.accountCreationUrl", "http://localhost:1234/user-account-creation")
    };

    @BeforeClass
    public static void setUp() {
        stubFor(post("/match").willReturn(okJson("{\"result\": \"match\"}")));
        stubFor(post("/user-account-creation").willReturn(okJson("{\"result\": \"success\"}")));
    }

    @Before
    public void reset() {
        wireMockRule.resetRequests();
    }

    protected abstract DropwizardAppRule<MatchingServiceAdapterConfiguration> getAppRule();

    @Before
    public void setUris() {
        MATCHING_SERVICE_URI = "http://localhost:" + getAppRule().getLocalPort() + "/matching-service/POST";
        UNKNOWN_USER_URI = "http://localhost:" + getAppRule().getLocalPort() + "/unknown-user-attribute-query";
    }

    protected void assertThatRequestThatWillBeSentIsEquivalentToFile(AttributeQuery attributeQuery, Path file) throws Exception {
      assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, file, MATCHING_SERVICE_URI);
    }

    protected void assertThatRequestThatWillBeSentIsEquivalentToFile(AttributeQuery attributeQuery, Path file, String uri) throws Exception {
        makeAttributeQueryRequest(uri, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        List<LoggedRequest> requests = findAll(RequestPatternBuilder.allRequests());

        assertThat(requests.size()).isEqualTo(1);

        Map requestSent = objectMapper.readValue(requests.get(0).getBodyAsString(), Map.class);
        Map requestExpected = objectMapper.readValue(readExpectedJson(file), Map.class);

        // These first two are not strictly necessary, as they are implied by the recursive field comparison,
        // but they mean that we see a much more useful error message if field names are different.
        assertThat(requestSent.keySet()).containsExactlyInAnyOrder(requestExpected.keySet().toArray());
        if (requestExpected.containsKey("matchingDataset")) {
            assertThat(((Map)requestSent.get("matchingDataset")).keySet()).containsExactlyInAnyOrder(((Map)requestExpected.get("matchingDataset")).keySet().toArray());
        }

        assertThat(requestSent).isEqualToComparingFieldByFieldRecursively(requestExpected);
    }

    private String readExpectedJson(Path filePath) throws Exception {
        return makeDateReplacements(new String(Files.readAllBytes(filePath)));
    }

    private String makeDateReplacements(String input) {
        return input.replace("%yesterdayDate%", getDateReplacement(yesterday).toString())
            .replace("%within405days-100days%", getDateReplacement(inRange405to100).toString())
            .replace("%within405days-101days%", getDateReplacement(inRange405to101).toString())
            .replace("%within405days-200days%", getDateReplacement(inRange405to200).toString())
            .replace("%within180days-100days%", getDateReplacement(inRange180to100).toString())
            .replace("%within180days-101days%", getDateReplacement(inRange180to101).toString())
            .replace("%within180days-150days%", getDateReplacement(inRange180to150).toString());
    }

    protected DateTime getDateReplacement(Integer daysToSubtract) {
        return new DateTime(DateTimeZone.UTC)
            .withTime(0, 0, 0, 0)
            .minusDays(daysToSubtract);
    }
}
