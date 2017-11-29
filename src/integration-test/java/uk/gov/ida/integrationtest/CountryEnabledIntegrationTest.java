package uk.gov.ida.integrationtest;

import helpers.JerseyClientConfigurationBuilder;
import httpstub.HttpStubRule;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.ConfigOverride;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import org.apache.http.HttpStatus;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA1;
import org.opensaml.xmlsec.signature.Signature;
import org.w3c.dom.Document;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppRule;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.rest.Urls;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder;
import uk.gov.ida.saml.serializers.XmlObjectToElementTransformer;
import uk.gov.ida.shared.utils.xml.XmlUtils;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.net.URI;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.opensaml.saml.saml2.core.StatusCode.RESPONDER;
import static org.opensaml.saml.saml2.core.StatusCode.SUCCESS;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aSubjectWithEncryptedAssertions;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.anEidasEncryptedAssertion;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.anEidasEncryptedAssertionWithInvalidSignature;
import static uk.gov.ida.integrationtest.helpers.RequestHelper.makeAttributeQueryRequest;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryAssertionValidator.generateInvalidSignatureMessage;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator.DEFAULT_INVALID_SIGNATURE_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator.IDENTITY_ASSERTION;
import static uk.gov.ida.saml.core.domain.SamlStatusCode.MATCH;
import static uk.gov.ida.saml.core.domain.SamlStatusCode.NO_MATCH;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HEADLESS_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HEADLESS_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_SECONDARY_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_IDP_ONE;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anEidasAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anEidasAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static uk.gov.ida.saml.core.test.matchers.SignableSAMLObjectBaseMatcher.signedBy;

public class CountryEnabledIntegrationTest {

    private static final String REQUEST_ID = "a-request-id";
    private static final String MATCHING_REQUEST_PATH = "/matching-request";

    private static Client client;

    private final SignatureAlgorithm signatureAlgorithmForHub = new SignatureRSASHA1();
    private final DigestAlgorithm digestAlgorithmForHub = new DigestSHA256();

    @ClassRule
    public static final HttpStubRule localMatchingService = new HttpStubRule();

    @ClassRule
    public static final DropwizardAppRule<MatchingServiceAdapterConfiguration> msaApplicationRule = new MatchingServiceAdapterAppRule(true,
        ConfigOverride.config("localMatchingService.matchUrl", "http://localhost:" + localMatchingService.getPort() + MATCHING_REQUEST_PATH));

    public static String MSA_MATCHING_URL;

    @BeforeClass
    public static void beforeClass() {
        JerseyClientConfiguration jerseyClientConfiguration = JerseyClientConfigurationBuilder.aJerseyClientConfiguration().withTimeout(Duration.seconds(10)).build();
        client = new JerseyClientBuilder(msaApplicationRule.getEnvironment()).using(jerseyClientConfiguration).build(CountryEnabledIntegrationTest.class.getSimpleName());
        MSA_MATCHING_URL = "http://localhost:" + msaApplicationRule.getLocalPort() + Urls.MatchingServiceAdapterUrls.MATCHING_SERVICE_ROOT
            + Urls.MatchingServiceAdapterUrls.MATCHING_SERVICE_MATCH_REQUEST_PATH;
    }

    @Before
    public void setup() throws Exception {
        localMatchingService.reset();
        localMatchingService.register(MATCHING_REQUEST_PATH, 200, "application/json", "{\"result\": \"match\"}");
    }

    @Test
    public void shouldFetchCountryMetadataWhenCountryConfigExists() {
        assertThat(msaApplicationRule.getEnvironment().healthChecks().getNames()).contains("CountryMetadataHealthCheck");
    }

    @Test
    public void shouldProcessMatchedEidasAttributeQueryRequestSuccessfully() {
        org.opensaml.saml.saml2.core.Response response = makeAttributeQueryRequest(MSA_MATCHING_URL, aValidAttributeQuery(), signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(SUCCESS);
        assertThat(response.getStatus().getStatusCode().getStatusCode().getValue()).isEqualTo(MATCH);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    @Test
    public void shouldProcessUnmatchedEidasAttributeQueryRequestSuccessfully() throws Exception {
        localMatchingService.reset();
        localMatchingService.register(MATCHING_REQUEST_PATH, 200, "application/json", "{\"result\": \"no-match\"}");
        org.opensaml.saml.saml2.core.Response response = makeAttributeQueryRequest(MSA_MATCHING_URL, aValidAttributeQuery(), signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(RESPONDER);
        assertThat(response.getStatus().getStatusCode().getStatusCode().getValue()).isEqualTo(NO_MATCH);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    @Test
    public void shouldNotProcessEidasAttributeQueryRequestContainingItsInvalidSignature() {
        String issuerId = HUB_ENTITY_ID;
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(issuerId).build())
            .withSubject(
                aSubjectWithEncryptedAssertions(
                    singletonList(anEidasEncryptedAssertion(STUB_IDP_ONE)), REQUEST_ID, HUB_ENTITY_ID)
            )
            .withSignature(
                aSignature()
                    .withSigningCredential(
                        new TestCredentialFactory(
                            HEADLESS_RP_PUBLIC_SIGNING_CERT,
                            HEADLESS_RP_PRIVATE_SIGNING_KEY
                        ).getSigningCredential()
                    ).build()
            )
            .build();

        Response response = postResponse(MSA_MATCHING_URL, attributeQuery);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertThat(response.readEntity(String.class)).contains(DEFAULT_INVALID_SIGNATURE_MESSAGE.getRenderedMessage());
    }

    @Test
    public void shouldNotProcessEidasAttributeQueryRequestContainingItsAssertionInvalidSignature() {
        String issuerId = HUB_ENTITY_ID;
        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(issuerId).build())
            .withSubject(
                aSubjectWithEncryptedAssertions(
                    singletonList(anEidasEncryptedAssertionWithInvalidSignature()), REQUEST_ID, HUB_ENTITY_ID)
            )
            .withSignature(
                aSignature()
                    .withSigningCredential(
                        new TestCredentialFactory(
                            HUB_TEST_PUBLIC_SIGNING_CERT,
                            HUB_TEST_PRIVATE_SIGNING_KEY
                        ).getSigningCredential()
                    ).build()
            )
            .build();

        Response response = postResponse(MSA_MATCHING_URL, attributeQuery);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertThat(response.readEntity(String.class)).contains(generateInvalidSignatureMessage(IDENTITY_ASSERTION).getRenderedMessage());
    }

    private AttributeQuery aValidAttributeQuery() {
        String issuerId = HUB_ENTITY_ID;
        return AttributeQueryBuilder.anAttributeQuery()
            .withId(REQUEST_ID)
            .withIssuer(anIssuer().withIssuerId(issuerId).build())
            .withSubject(
                aSubjectWithEncryptedAssertions(
                    singletonList(
                        anAssertion()
                            .withSubject(
                                aSubject().withSubjectConfirmation(
                                    aSubjectConfirmation().withSubjectConfirmationData(
                                        aSubjectConfirmationData()
                                            .withInResponseTo(REQUEST_ID)
                                            .build())
                                        .build())
                                    .build()
                            )
                            .withIssuer(
                                anIssuer()
                                    .withIssuerId(STUB_IDP_ONE)
                                    .build())
                            .addAttributeStatement(anEidasAttributeStatement().build())
                            .addAuthnStatement(anEidasAuthnStatement().build())
                            .withSignature(aValidSignature())
                            .withConditions(aConditions())
                            .buildWithEncrypterCredential(
                                new TestCredentialFactory(
                                    TEST_RP_MS_PUBLIC_ENCRYPTION_CERT,
                                    TEST_RP_MS_PRIVATE_ENCRYPTION_KEY
                                ).getEncryptingCredential()
                            )
                    ),
                    REQUEST_ID, HUB_ENTITY_ID)
            )
            .withSignature(
                aSignature()
                    .withSigningCredential(
                        new TestCredentialFactory(
                            HUB_TEST_PUBLIC_SIGNING_CERT,
                            HUB_TEST_PRIVATE_SIGNING_KEY
                        ).getSigningCredential()
                    ).build()
            )
            .build();
    }

    private Response postResponse(String url, AttributeQuery attributeQuery) {
        Document soapEnvelope = new SoapMessageManager().wrapWithSoapEnvelope(new XmlObjectToElementTransformer<>().apply(attributeQuery));
        String xmlString = XmlUtils.writeToString(soapEnvelope);

        URI uri = UriBuilder.fromPath(url).build();
        return client
            .target(uri.toASCIIString())
            .request()
            .post(Entity.entity(xmlString, MediaType.TEXT_XML));
    }

    private static Signature aValidSignature() {
        return aSignature()
            .withSigningCredential(
                new TestCredentialFactory(
                    STUB_IDP_PUBLIC_PRIMARY_CERT,
                    STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY
                ).getSigningCredential()
            ).build();
    }

    private static Conditions aConditions() {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(DateTime.now());
        conditions.setNotOnOrAfter(DateTime.now().plusMinutes(10));
        AudienceRestriction audienceRestriction = new AudienceRestrictionBuilder().buildObject();
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI(HUB_SECONDARY_ENTITY_ID);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }

}
