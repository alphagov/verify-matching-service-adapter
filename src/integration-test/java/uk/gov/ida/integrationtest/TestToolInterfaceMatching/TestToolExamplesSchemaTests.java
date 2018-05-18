package uk.gov.ida.integrationtest.TestToolInterfaceMatching;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.util.ISO8601DateFormat;
import com.github.tomakehurst.wiremock.junit.WireMockClassRule;
import com.github.tomakehurst.wiremock.matching.RequestPatternBuilder;
import com.github.tomakehurst.wiremock.verification.LoggedRequest;
import io.dropwizard.jackson.Jackson;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA1;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppRule;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;

import static com.github.tomakehurst.wiremock.client.WireMock.findAll;
import static com.github.tomakehurst.wiremock.client.WireMock.okJson;
import static com.github.tomakehurst.wiremock.client.WireMock.post;
import static com.github.tomakehurst.wiremock.client.WireMock.stubFor;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aMatchingDatasetAssertion;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aCycle3Assertion;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aSubjectWithAssertions;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.anAuthnStatementAssertion;
import static uk.gov.ida.integrationtest.helpers.RequestHelper.makeAttributeQueryRequest;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.AddressAttributeBuilder_1_1.anAddressAttribute;
import static uk.gov.ida.saml.core.test.builders.AddressAttributeValueBuilder_1_1.anAddressAttributeValue;
import static uk.gov.ida.saml.core.test.builders.DateAttributeBuilder_1_1.aDate_1_1;
import static uk.gov.ida.saml.core.test.builders.DateAttributeValueBuilder.aDateValue;
import static uk.gov.ida.saml.core.test.builders.GenderAttributeBuilder_1_1.aGender_1_1;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.PersonNameAttributeBuilder_1_1.aPersonName_1_1;
import static uk.gov.ida.saml.core.test.builders.PersonNameAttributeValueBuilder.aPersonNameValue;

public class TestToolExamplesSchemaTests {
    private final SignatureAlgorithm signatureAlgorithmForHub = new SignatureRSASHA1();
    private final DigestAlgorithm digestAlgorithmForHub = new DigestSHA256();
    private static final String REQUEST_ID = "default-match-id";
    private static final String PID = "default-pid";
    private static final ObjectMapper objectMapper = Jackson.newObjectMapper().setDateFormat(ISO8601DateFormat.getDateInstance());
    private static final Integer yesterday = 1;
    private static final Integer inRange405to100 = 100;
    private static final Integer inRange405to101 = 150;
    private static final Integer inRange405to200 = 400;
    private static final Integer inRange180to100 = 100;
    private static final Integer inRange180to101 = 140;
    private static final Integer inRange180to150 = 160;

    @ClassRule
    public static WireMockClassRule wireMockRule = new WireMockClassRule(wireMockConfig().port(1234));

    @ClassRule
    public static final DropwizardAppRule<MatchingServiceAdapterConfiguration> applicationRule = new MatchingServiceAdapterAppRule(
        ConfigOverride.config("localMatchingService.matchUrl", "http://localhost:1234/match"),
        ConfigOverride.config("localMatchingService.accountCreationUrl", "http://localhost:1234/user-account-creation")
    );

    private final String MATCHING_SERVICE_URI = "http://localhost:" + applicationRule.getLocalPort() + "/matching-service/POST";
    private final String UNKNOWN_USER_URI = "http://localhost:" + applicationRule.getLocalPort() + "/unknown-user-attribute-query";

    @BeforeClass
    public static void setUp() {
        stubFor(post("/match").willReturn(okJson("{\"result\": \"match\"}")));
        stubFor(post("/user-account-creation").willReturn(okJson("{\"result\": \"success\"}")));
    }

    @Before
    public void reset() {
        wireMockRule.resetRequests();
    }

    @Test
    public void shouldProduceLoA2SimpleCase() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(asList(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(asList(
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(true)
                            .withFrom(null)
                            .withTo(null)
                            .build())
                        .buildAsFirstname(),
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Dou")
                            .withVerified(true)
                            .withFrom(new DateTime(2010, 1, 20, 0, 0, DateTimeZone.UTC))
                            .withTo(null)
                            .build())
                        .buildAsSurname(),
                    aDate_1_1().addValue(
                        aDateValue()
                            .withValue("1980-05-24")
                            .withVerified(true)
                            .withFrom(null)
                            .withTo(null)
                            .build()).buildAsDateOfBirth(),
                    anAddressAttribute().addAddress(
                        anAddressAttributeValue()
                            .addLines(asList("10 George Street"))
                            .withFrom(new DateTime(2005, 5, 14, 0, 0, DateTimeZone.UTC))
                            .withInternationalPostcode("GB1 2PF")
                            .withPostcode("GB1 2PF")
                            .withUprn("833F1187-9F33-A7E27B3F211E")
                            .withVerified(true)
                            .withTo(null)
                            .build())
                        .buildCurrentAddress()
                ), false, REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/legacy/LoA2-Minimum_data_set.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    @Test
    public void shouldProduceLoA1SimpleCase() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(asList(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_1_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(asList(
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(true)
                            .withFrom(null)
                            .withTo(null)
                            .build())
                        .buildAsFirstname(),
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Dou")
                            .withVerified(true)
                            .withFrom(new DateTime(2015, 5, 14, 0, 0, DateTimeZone.UTC))
                            .withTo(null)
                            .build())
                        .buildAsSurname(),
                    aDate_1_1().addValue(
                        aDateValue()
                            .withValue("1980-05-24")
                            .withVerified(true)
                            .withFrom(null)
                            .withTo(null)
                            .build()).buildAsDateOfBirth(),
                    anAddressAttribute().addAddress(
                        anAddressAttributeValue()
                            .addLines(asList("10 George Street"))
                            .withFrom(new DateTime(2005, 5, 14, 0, 0, DateTimeZone.UTC))
                            .withInternationalPostcode("GB1 2PF")
                            .withPostcode("GB1 2PF")
                            .withUprn("833F1187-9F33-A7E27B3F211E")
                            .withVerified(true)
                            .withTo(null)
                            .build())
                        .buildCurrentAddress()
                ), false, REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/legacy/LoA1-Minimum_data_set.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    @Test
    public void shouldProduceLoA1ExtensiveCase() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(asList(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_1_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(asList(
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(false)
                            .withFrom(null)
                            .withTo(null)
                            .build())
                        .buildAsFirstname(),
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Bob Rob")
                            .withVerified(false)
                            .withFrom(null)
                            .withTo(null)
                            .build())
                        .buildAsMiddlename(),
                    aPersonName_1_1()
                        .addValue(
                            aPersonNameValue()
                                .withValue("Fred")
                                .withVerified(false)
                                .withFrom(new DateTime(1980, 5, 24, 0, 0, DateTimeZone.UTC))
                                .withTo(new DateTime(1987, 1, 20, 0, 0, DateTimeZone.UTC))
                                .build()
                        ).addValue(
                            aPersonNameValue()
                                .withValue("Dou")
                                .withVerified(false)
                                .withFrom(getDateReplacement(yesterday))
                                .withTo(null)
                                .build()
                        ).addValue(
                            aPersonNameValue()
                                .withValue("John")
                                .withVerified(true)
                                .withFrom(new DateTime(2003, 5, 24, 0, 0, DateTimeZone.UTC))
                                .withTo(new DateTime(2004, 1, 20, 0, 0, DateTimeZone.UTC))
                                .build()
                        ).addValue(
                            aPersonNameValue()
                                .withValue("Joe")
                                .withVerified(true)
                                .withFrom(new DateTime(2005, 5, 24, 0, 0, DateTimeZone.UTC))
                                .withTo(getDateReplacement(inRange405to100))
                                .build()
                        ).addValue(
                            aPersonNameValue()
                                .withValue("Simon")
                                .withVerified(false)
                                .withFrom(getDateReplacement(inRange405to101))
                                .withTo(getDateReplacement(inRange405to200))
                                .build()
                        )
                        .buildAsSurname(),
                    aGender_1_1().withValue("Male").withVerified(false).withFrom(null).withTo(null).build(),
                    aDate_1_1().addValue(
                        aDateValue()
                            .withValue("1980-05-24")
                            .withVerified(true)
                            .withFrom(null)
                            .withTo(null)
                            .build()).buildAsDateOfBirth(),
                    anAddressAttribute().addAddress(
                        anAddressAttributeValue()
                            .addLines(asList("2323 George Street"))
                            .withFrom(getDateReplacement(yesterday))
                            .withInternationalPostcode("GB1 5PP")
                            .withPostcode("GB1 2PP")
                            .withUprn("7D68E096-5510-B3844C0BA3FD")
                            .withVerified(false)
                            .withTo(null)
                            .build())
                        .buildCurrentAddress(),
                    anAddressAttribute()
                        .addAddress(
                            anAddressAttributeValue()
                                .addLines(asList("10 George Street"))
                                .withFrom(new DateTime(2005, 5, 14, 0, 0, DateTimeZone.UTC))
                                .withTo(new DateTime(2007, 5, 14, 0, 0, DateTimeZone.UTC))
                                .withPostcode("GB1 2PF")
                                .withInternationalPostcode("GB1 2PF")
                                .withUprn("833F1187-9F33-A7E27B3F211E")
                                .withVerified(true)
                                .build()
                        ).addAddress(
                            anAddressAttributeValue()
                                .addLines(asList("344 George Street"))
                                .withFrom(new DateTime(2009, 5, 24, 0, 0, DateTimeZone.UTC))
                                .withTo(getDateReplacement(inRange405to100))
                                .withPostcode("GB1 2PP")
                                .withInternationalPostcode("GB1 2PP")
                                .withUprn("7D68E096-5510-B3844C0BA3FD")
                                .withVerified(true)
                                .build()
                        ).addAddress(
                            anAddressAttributeValue()
                                .addLines(asList("67676 George Street"))
                                .withFrom(getDateReplacement(inRange405to101))
                                .withTo(getDateReplacement(inRange405to200))
                                .withPostcode("GB1 2PP")
                                .withInternationalPostcode("GB1 3PP")
                                .withUprn("7D68E096-5510-B3844C0BA3FD")
                                .withVerified(false)
                                .build()
                        ).addAddress(
                            anAddressAttributeValue()
                                .addLines(asList("46244 George Street"))
                                .withFrom(new DateTime(1980, 5, 24, 0, 0, DateTimeZone.UTC))
                                .withTo(new DateTime(1987, 5, 24, 0, 0, DateTimeZone.UTC))
                                .withPostcode("GB1 2PP")
                                .withInternationalPostcode("GB1 3PP")
                                .withUprn("7D68E096-5510-B3844C0BA3FD")
                                .withVerified(false)
                                .build()
                        ).addAddress(
                            anAddressAttributeValue()
                                .addLines(asList("Flat , Alberta Court", "36 Harrods Road", "New Berkshire", "Berkshire", "Cambria", "Europe"))
                                .withFrom(new DateTime(1987, 5, 24, 0, 0, DateTimeZone.UTC))
                                .withTo(new DateTime(1989, 5, 24, 0, 0, DateTimeZone.UTC))
                                .withPostcode(null)
                                .withInternationalPostcode(null)
                                .withUprn(null)
                                .withVerified(false)
                                .build()
                        ).buildPreviousAddress()
                ), false, REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/legacy/LoA1-Extended_data_set.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    @Test
    public void shouldProduceLoA2ExtensiveCase() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(asList(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(asList(
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(false)
                            .withFrom(null)
                            .withTo(null)
                            .build())
                        .buildAsFirstname(),
                    aPersonName_1_1().addValue(
                        aPersonNameValue()
                            .withValue("Bob Rob")
                            .withVerified(false)
                            .withFrom(null)
                            .withTo(null)
                            .build())
                        .buildAsMiddlename(),
                    aPersonName_1_1()
                        .addValue(
                            aPersonNameValue()
                                .withValue("Fred")
                                .withVerified(false)
                                .withFrom(new DateTime(1980, 5, 24, 0, 0, DateTimeZone.UTC))
                                .withTo(new DateTime(1987, 1, 20, 0, 0, DateTimeZone.UTC))
                                .build()
                        ).addValue(
                        aPersonNameValue()
                            .withValue("Dou")
                            .withVerified(false)
                            .withFrom(getDateReplacement(yesterday))
                            .withTo(null)
                            .build()
                    ).addValue(
                        aPersonNameValue()
                            .withValue("John")
                            .withVerified(true)
                            .withFrom(new DateTime(2003, 5, 24, 0, 0, DateTimeZone.UTC))
                            .withTo(new DateTime(2004, 1, 20, 0, 0, DateTimeZone.UTC))
                            .build()
                    ).addValue(
                        aPersonNameValue()
                            .withValue("Joe")
                            .withVerified(true)
                            .withFrom(new DateTime(2005, 5, 24, 0, 0, DateTimeZone.UTC))
                            .withTo(getDateReplacement(inRange180to100))
                            .build()
                    ).addValue(
                        aPersonNameValue()
                            .withValue("Simon")
                            .withVerified(false)
                            .withFrom(getDateReplacement(inRange180to101))
                            .withTo(getDateReplacement(inRange180to150))
                            .build()
                    )
                        .buildAsSurname(),
                    aGender_1_1().withValue("Male").withVerified(false).withFrom(null).withTo(null).build(),
                    aDate_1_1().addValue(
                        aDateValue()
                            .withValue("1980-05-24")
                            .withVerified(true)
                            .withFrom(null)
                            .withTo(null)
                            .build()).buildAsDateOfBirth(),
                    anAddressAttribute().addAddress(
                        anAddressAttributeValue()
                            .addLines(asList("2323 George Street"))
                            .withFrom(getDateReplacement(yesterday))
                            .withInternationalPostcode("GB1 5PP")
                            .withPostcode("GB1 2PP")
                            .withUprn("7D68E096-5510-B3844C0BA3FD")
                            .withVerified(false)
                            .withTo(null)
                            .build())
                        .buildCurrentAddress(),
                    anAddressAttribute()
                        .addAddress(
                            anAddressAttributeValue()
                                .addLines(asList("10 George Street"))
                                .withFrom(new DateTime(2005, 5, 14, 0, 0, DateTimeZone.UTC))
                                .withTo(new DateTime(2007, 5, 14, 0, 0, DateTimeZone.UTC))
                                .withPostcode("GB1 2PF")
                                .withInternationalPostcode("GB1 2PF")
                                .withUprn("833F1187-9F33-A7E27B3F211E")
                                .withVerified(true)
                                .build()
                        ).addAddress(
                        anAddressAttributeValue()
                            .addLines(asList("344 George Street"))
                            .withFrom(new DateTime(2009, 5, 24, 0, 0, DateTimeZone.UTC))
                            .withTo(getDateReplacement(inRange405to100))
                            .withPostcode("GB1 2PP")
                            .withInternationalPostcode("GB1 2PP")
                            .withUprn("7D68E096-5510-B3844C0BA3FD")
                            .withVerified(true)
                            .build()
                    ).addAddress(
                        anAddressAttributeValue()
                            .addLines(asList("67676 George Street"))
                            .withFrom(getDateReplacement(inRange405to101))
                            .withTo(getDateReplacement(inRange405to200))
                            .withPostcode("GB1 2PP")
                            .withInternationalPostcode("GB1 3PP")
                            .withUprn("7D68E096-5510-B3844C0BA3FD")
                            .withVerified(false)
                            .build()
                    ).addAddress(
                        anAddressAttributeValue()
                            .addLines(asList("56563 George Street"))
                            .withFrom(new DateTime(1980, 5, 24, 0, 0, DateTimeZone.UTC))
                            .withTo(new DateTime(1987, 5, 24, 0, 0, DateTimeZone.UTC))
                            .withPostcode("GB1 2PP")
                            .withInternationalPostcode("GB1 3PP")
                            .withUprn("7D68E096-5510-B3844C0BA3FD")
                            .withVerified(false)
                            .build()
                    ).addAddress(
                        anAddressAttributeValue()
                            .addLines(asList("Flat , Alberta Court", "36 Harrods Road", "New Berkshire", "Berkshire", "Cambria", "Europe"))
                            .withFrom(new DateTime(1987, 5, 24, 0, 0, DateTimeZone.UTC))
                            .withTo(new DateTime(1989, 5, 24, 0, 0, DateTimeZone.UTC))
                            .withPostcode(null)
                            .withInternationalPostcode(null)
                            .withUprn(null)
                            .withVerified(false)
                            .build()
                    ).buildPreviousAddress()
                ), false, REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        Path path = Paths.get("verify-matching-service-test-tool/src/main/resources/legacy/LoA2-Extended_data_set.json");

        assertThatRequestThatWillBeSentIsEquivalentToFile(attributeQuery, path);
    }

    @Test
    public void shouldProduceUserAccountCreationJson() throws Exception {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withAttributes(asList())
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(asList(
                anAuthnStatementAssertion(IdaAuthnContext.LEVEL_1_AUTHN_CTX, REQUEST_ID),
                aMatchingDatasetAssertion(asList(), false, REQUEST_ID)
            ), REQUEST_ID, HUB_ENTITY_ID, PID))
            .build();

        makeAttributeQueryRequest(UNKNOWN_USER_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        List<LoggedRequest> requests = findAll(RequestPatternBuilder.allRequests());
        assertThat(requests.size()).isEqualTo(1);

        Path filePath = Paths.get("verify-matching-service-test-tool/src/main/resources/legacy/user_account_creation.json");

        Map requestSent = objectMapper.readValue(requests.get(0).getBodyAsString(), Map.class);
        Map requestExpected = objectMapper.readValue(readExpectedJson(filePath), Map.class);

        assertThat(requestSent).isEqualToComparingFieldByFieldRecursively(requestExpected);
    }

    private void assertThatRequestThatWillBeSentIsEquivalentToFile(AttributeQuery attributeQuery, Path filePath) throws Exception {
        makeAttributeQueryRequest(MATCHING_SERVICE_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        List<LoggedRequest> requests = findAll(RequestPatternBuilder.allRequests());

        assertThat(requests.size()).isEqualTo(1);

        Map requestSent = objectMapper.readValue(requests.get(0).getBodyAsString(), Map.class);
        Map requestExpected = objectMapper.readValue(readExpectedJson(filePath), Map.class);

        // These first two are not strictly necessary, as they are implied by the recursive field comparison,
        // but they mean that we see a much more useful error message if field names are different.
        assertThat(requestSent.keySet()).containsExactlyInAnyOrder(requestExpected.keySet().toArray());
        assertThat(((Map)requestSent.get("matchingDataset")).keySet()).containsExactlyInAnyOrder(((Map)requestExpected.get("matchingDataset")).keySet().toArray());

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

    private DateTime getDateReplacement(Integer daysToSubtract) {
        return new DateTime(DateTimeZone.UTC)
            .withTime(0, 0, 0, 0)
            .minusDays(daysToSubtract);
    }
}
