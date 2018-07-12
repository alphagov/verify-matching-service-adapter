package uk.gov.ida.integrationtest;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.classic.spi.LoggingEvent;
import ch.qos.logback.core.Appender;
import com.fasterxml.jackson.core.JsonProcessingException;
import httpstub.HttpStubRule;
import io.dropwizard.testing.ConfigOverride;
import org.glassfish.jersey.client.JerseyClientBuilder;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.ClassRule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA1;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import uk.gov.ida.common.CommonUrls;
import uk.gov.ida.common.ServiceNameDto;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppRule;
import uk.gov.ida.matchingserviceadapter.resources.MatchingServiceResource;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.Optional;

import static java.util.Arrays.asList;
import static javax.ws.rs.core.MediaType.TEXT_XML_TYPE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.opensaml.saml.saml2.core.StatusCode.REQUESTER;
import static org.opensaml.saml.saml2.core.StatusCode.RESPONDER;
import static org.opensaml.saml.saml2.core.StatusCode.SUCCESS;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aMatchingDatasetAssertion;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aMatchingDatasetAssertionWithSignature;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aSubjectWithAssertions;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.anAuthnStatementAssertion;
import static uk.gov.ida.integrationtest.helpers.RequestHelper.getAttributeQueryToElementTransformer;
import static uk.gov.ida.integrationtest.helpers.RequestHelper.makeAttributeQueryRequest;
import static uk.gov.ida.saml.core.domain.SamlStatusCode.HEALTHY;
import static uk.gov.ida.saml.core.domain.SamlStatusCode.MATCH;
import static uk.gov.ida.saml.core.domain.SamlStatusCode.NO_MATCH;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_SECONDARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_IDP_ONE;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_IDP_TWO;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.aCycle3DatasetAssertion;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.IPAddressAttributeBuilder.anIPAddress;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.PersonNameAttributeBuilder_1_1.aPersonName_1_1;
import static uk.gov.ida.saml.core.test.builders.PersonNameAttributeValueBuilder.aPersonNameValue;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.matchers.SignableSAMLObjectBaseMatcher.signedBy;

public class VerifyMatchingIntegrationTest {
    private static final String REQUEST_ID = "default-request-id";
    private static final String MATCHING_REQUEST_PATH = "/matching-request";

    @ClassRule
    public static final HttpStubRule localMatchingService = new HttpStubRule();

    @ClassRule
    public static final MatchingServiceAdapterAppRule applicationRule = new MatchingServiceAdapterAppRule(
            ConfigOverride.config("localMatchingService.matchUrl", "http://localhost:" + localMatchingService.getPort() + MATCHING_REQUEST_PATH)
    );

    private final String MATCHING_SERVICE_URI = "http://localhost:" + applicationRule.getLocalPort() + "/matching-service/POST";
    private Client client = JerseyClientBuilder.createClient();

    private final SignatureAlgorithm signatureAlgorithmForHub = new SignatureRSASHA1();
    private final DigestAlgorithm digestAlgorithmForHub = new DigestSHA256();

    @SuppressWarnings("unchecked")
    @Mock
    private Appender<ILoggingEvent> appender = mock(Appender.class);

    private ch.qos.logback.classic.Logger logger = (ch.qos.logback.classic.Logger) LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);

    @Mock
    private ArgumentCaptor<LoggingEvent> argumentCaptor = ArgumentCaptor.forClass(LoggingEvent.class);

    @Before
    public void setup() throws Exception {
        localMatchingService.reset();
        localMatchingService.register(MATCHING_REQUEST_PATH, 200, "application/json", "{\"result\": \"match\"}");

        logger.addAppender(appender);
    }

    @After
    public void tearDown() {
        logger.detachAppender(appender);
    }

    @Test
    public void shouldReturnASuccessResponseWhenLocalMatchingServiceReturnsAMatch() {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
                .withId(REQUEST_ID)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion(REQUEST_ID),
                        aDefaultMatchingDatasetAssertion()), REQUEST_ID, HUB_ENTITY_ID))
                .build();
        Response response = makeAttributeQueryRequest(MATCHING_SERVICE_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(SUCCESS);
        assertThat(response.getStatus().getStatusCode().getStatusCode().getValue()).isEqualTo(MATCH);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));

        assertMatchStatusLogMessage("default-request-id", MatchingServiceResponseDto.MATCH);
    }

    @Test
    public void shouldReturnASuccessResponseWhenLocalMatchingServiceReturnsNoMatch() throws JsonProcessingException {
        localMatchingService.reset();
        localMatchingService.register(MATCHING_REQUEST_PATH, 200, "application/json", "{\"result\": \"no-match\"}");

        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
                .withId(REQUEST_ID)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion(REQUEST_ID),
                        aDefaultMatchingDatasetAssertion()), REQUEST_ID, HUB_ENTITY_ID))
                .build();
        Response response = makeAttributeQueryRequest(MATCHING_SERVICE_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(RESPONDER);
        assertThat(response.getStatus().getStatusCode().getStatusCode().getValue()).isEqualTo(NO_MATCH);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));

        assertMatchStatusLogMessage("default-request-id", MatchingServiceResponseDto.NO_MATCH);
    }

    @Test
    public void shouldReturnSuccessResponseWhenLocalMatchingServiceMatchesWithCycle3Data() {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
                .withId(REQUEST_ID)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion(REQUEST_ID),
                        aDefaultMatchingDatasetAssertion(),
                        aCycle3DatasetAssertion("cycle-3-name", "cycle-3-value").buildUnencrypted()
                ), REQUEST_ID, HUB_ENTITY_ID))
                .build();

        Response response = makeAttributeQueryRequest(MATCHING_SERVICE_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(SUCCESS);
        assertThat(response.getStatus().getStatusCode().getStatusCode().getValue()).isEqualTo(MATCH);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));

        assertMatchStatusLogMessage(REQUEST_ID, MatchingServiceResponseDto.MATCH);
    }

    @Test
    public void shouldReturnErrorResponseWhenIncorrectlySignedAssertionProvided() {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
                .withId(REQUEST_ID)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion(REQUEST_ID),
                        aMatchingDatasetAssertionWithSignature(Collections.emptyList(),
                                aSignature()
                                        .withSigningCredential(
                                                new TestCredentialFactory(
                                                        TestCertificateStrings.STUB_IDP_PUBLIC_SECONDARY_CERT,
                                                        STUB_IDP_PUBLIC_SECONDARY_PRIVATE_KEY
                                                ).getSigningCredential()
                                        ).build(), false, REQUEST_ID
                        ),
                        aCycle3DatasetAssertion("cycle-3-name", "cycle-3-value").buildUnencrypted()
                ), REQUEST_ID, HUB_ENTITY_ID))
                .build();

        Response response = makeAttributeQueryRequest(MATCHING_SERVICE_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(REQUESTER);
        assertThat(response.getStatus().getStatusMessage().getMessage()).isEqualTo("SAML Validation Specification: Signature was not valid.\n" +
                "DocumentReference{documentName='Hub Service Profile 1.1a', documentSection=''}");
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    @Test
    public void shouldReturnErrorResponseWhenSentIncorrectlySignedAttributeQuery() {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
                .withId(REQUEST_ID)
                .withIssuer(anIssuer().withIssuerId(TestEntityIds.STUB_IDP_ONE).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion(REQUEST_ID),
                        aDefaultMatchingDatasetAssertion()), REQUEST_ID, HUB_ENTITY_ID))
                .build();

        Response response = makeAttributeQueryRequest(MATCHING_SERVICE_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(REQUESTER);
        assertThat(response.getStatus().getStatusMessage().getMessage()).isEqualTo("SAML Validation Specification: Signature was not valid.\n" +
                "DocumentReference{documentName='Hub Service Profile 1.1a', documentSection=''}");
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    @Test
    public void shouldReturnErrorResponseWhenLocalMatchingServiceRespondsWithError() throws Exception {
        localMatchingService.reset();
        localMatchingService.register(MATCHING_REQUEST_PATH, 500, "application/json", "foo");
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
                .withId(REQUEST_ID)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion(REQUEST_ID),
                        aDefaultMatchingDatasetAssertion()), REQUEST_ID, HUB_ENTITY_ID))
                .build();
        Document attributeQueryDocument = getAttributeQueryToElementTransformer(signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID).apply(attributeQuery).getOwnerDocument();

        javax.ws.rs.core.Response response = client.target(MATCHING_SERVICE_URI).request()
                .post(Entity.entity(attributeQueryDocument, TEXT_XML_TYPE));

        assertThat(response.getStatus()).isEqualTo(500);
    }

    @Test
    public void shouldReturnErrorResponseWhenAnAttributeQueryContainsAnExpiredAssertion() {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
                .withId(REQUEST_ID)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAuthnStatementAssertion("default-request-id"),
                        aMatchingDatasetAssertion(Collections.emptyList(), true, REQUEST_ID)), REQUEST_ID, HUB_ENTITY_ID))
                .build();

        Response response = makeAttributeQueryRequest(MATCHING_SERVICE_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(REQUESTER);
        assertThat(response.getStatus().getStatusMessage().getMessage()).contains("Assertion is not valid on or after");
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    @Test
    public void shouldReturnErrorResponseWhenAnAttributeQueryContainsIdpAssertionsWithDifferentPids() {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
                .withId(REQUEST_ID)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAssertion()
                                .addAuthnStatement(anAuthnStatement().build())
                                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                                .addAttributeStatement(anAttributeStatement().addAttribute(anIPAddress().build()).build())
                                .withSubject(aSubject().withPersistentId("pid-one").build())
                                .buildUnencrypted(),
                        anAssertion()
                                .withId("mds-assertion")
                                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                                .withSubject(
                                        aSubject().withPersistentId("pid-two").build()
                                )
                                .addAttributeStatement(
                                        anAttributeStatement()
                                                .addAllAttributes(Collections.emptyList())
                                                .build()
                                )
                                .buildUnencrypted()
                ), REQUEST_ID, HUB_ENTITY_ID))
                .build();

        Response response = makeAttributeQueryRequest(MATCHING_SERVICE_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(REQUESTER);
        assertThat(response.getStatus().getStatusMessage().getMessage()).contains("assertions do not contain matching persistent identifiers");
    }

    @Test
    public void shouldReturnErrorResponseWhenAnAttributeQueryContainsIdpAssertionsWithDifferentIssuers() {
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
                .withId(REQUEST_ID)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
                .withSubject(aSubjectWithAssertions(asList(
                        anAssertion()
                                .addAuthnStatement(anAuthnStatement().build())
                                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                                .addAttributeStatement(anAttributeStatement().build())
                                .withSubject(aSubject().build())
                                .buildUnencrypted(),
                        anAssertion()
                                .withId("mds-assertion")
                                .withIssuer(anIssuer().withIssuerId(STUB_IDP_TWO).build())
                                .withSubject(aSubject().build())
                                .addAttributeStatement(
                                        anAttributeStatement()
                                                .addAllAttributes(Collections.emptyList())
                                                .build()
                                )
                                .buildUnencrypted()
                ), REQUEST_ID, HUB_ENTITY_ID))
                .build();

        Response response = makeAttributeQueryRequest(MATCHING_SERVICE_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(REQUESTER);
        assertThat(response.getStatus().getStatusMessage().getMessage()).contains("IDP matching dataset and authn statement assertions do not contain matching issuers");
    }


    @Test
    public void shouldReturnAHealthyResponseToAValidHealthcheckGivenValidMetadata() {
        AttributeQuery healthCheck = AttributeQueryBuilder.anAttributeQuery()
                .withId(REQUEST_ID)
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build()).build();

        Response response = makeAttributeQueryRequest(MATCHING_SERVICE_URI, healthCheck, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(SUCCESS);
        assertThat(response.getStatus().getStatusCode().getStatusCode().getValue()).isEqualTo(HEALTHY);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    @Test
    public void shouldReturnServiceNameWhenRequested() {
        ServiceNameDto serviceName = client.target("http://localhost:" + applicationRule.getLocalPort() + CommonUrls.SERVICE_NAME_ROOT)
                .request(MediaType.APPLICATION_JSON)
                .get(ServiceNameDto.class);

        assertThat(serviceName.getServiceName()).isEqualTo("matching-service-adapter");
    }

    private Assertion aDefaultMatchingDatasetAssertion() {
        return aMatchingDatasetAssertion(asList(
                aPersonName_1_1().addValue(aPersonNameValue().withValue("OldSurname2").withFrom(new DateTime(1990, 1, 30, 0, 0)).withTo(new DateTime(2000, 1, 29, 0, 0)).withVerified(true).build()).buildAsSurname(),
                aPersonName_1_1().addValue(aPersonNameValue().withValue("CurrentSurname").withVerified(true).build()).buildAsSurname(),
                aPersonName_1_1().addValue(aPersonNameValue().withValue("OldSurname1").withFrom(new DateTime(2000, 1, 30, 0, 0)).withTo(new DateTime(2010, 1, 30, 0, 0)).withVerified(true).build()).buildAsSurname()
                ),
                false, REQUEST_ID);
    }

    private void assertMatchStatusLogMessage(String requestId, String matchStatus) {
        verify(appender, atLeastOnce()).doAppend(argumentCaptor.capture());

        Optional<LoggingEvent> event = argumentCaptor.getAllValues()
                .stream()
                .filter(loggingEvent -> loggingEvent.getLoggerName().equals(MatchingServiceResource.class.getName()))
                .filter(loggingEvent -> loggingEvent.getFormattedMessage().equals("Result from matching service for id " + requestId + " is " + matchStatus))
                .findFirst();

        assertThat(event.isPresent()).isTrue();
    }

}