package uk.gov.ida.integrationtest;

import helpers.JerseyClientConfigurationBuilder;
import io.dropwizard.client.JerseyClientBuilder;
import io.dropwizard.client.JerseyClientConfiguration;
import io.dropwizard.testing.junit.DropwizardAppRule;
import io.dropwizard.util.Duration;
import org.apache.http.HttpStatus;
import org.junit.BeforeClass;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AttributeQuery;
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
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aSubjectWithEncryptedAssertions;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.anEidasEncryptedAssertion;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.anEidasEncryptedAssertionWithInvalidSignature;
import static uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers.EidasAttributeQueryToInboundMatchingServiceRequestTransformer.TODO_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryAssertionValidator.generateInvalidSignatureMessage;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator.DEFAULT_INVALID_SIGNATURE_MESSAGE;
import static uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator.IDENTITY_ASSERTION;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HEADLESS_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HEADLESS_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_IDP_ONE;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;

public class CountryEnabledIntegrationTest {

    private static final String REQUEST_ID = "a-request-id";

    private static Client client;

    @ClassRule
    public static final DropwizardAppRule<MatchingServiceAdapterConfiguration> msaApplicationRule = new MatchingServiceAdapterAppRule(true);
    public static String MSA_MATCHING_URL;

    @BeforeClass
    public static void beforeClass() {
        JerseyClientConfiguration jerseyClientConfiguration = JerseyClientConfigurationBuilder.aJerseyClientConfiguration().withTimeout(Duration.seconds(10)).build();
        client = new JerseyClientBuilder(msaApplicationRule.getEnvironment()).using(jerseyClientConfiguration).build(CountryEnabledIntegrationTest.class.getSimpleName());
        MSA_MATCHING_URL = "http://localhost:" + msaApplicationRule.getLocalPort() + Urls.MatchingServiceAdapterUrls.MATCHING_SERVICE_ROOT
            + Urls.MatchingServiceAdapterUrls.MATCHING_SERVICE_MATCH_REQUEST_PATH;
    }

    @Test
    public void shouldFetchCountryMetadataWhenCountryConfigExists() {

        assertThat(msaApplicationRule.getEnvironment().healthChecks().getNames()).contains("CountryMetadataHealthCheck");
    }

    @Test
    public void shouldProcessEidasAttributeQueryRequestSuccessfully() {
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
                                HUB_TEST_PUBLIC_SIGNING_CERT,
                                HUB_TEST_PRIVATE_SIGNING_KEY
                            ).getSigningCredential()
                        ).build()
                )
                .build();

        Response response = postResponse(MSA_MATCHING_URL, attributeQuery);

        assertThat(response.getStatus()).isEqualTo(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        assertThat(response.readEntity(String.class)).contains(TODO_MESSAGE);
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

    private Response postResponse(String url, AttributeQuery attributeQuery) {
        Document soapEnvelope = new SoapMessageManager().wrapWithSoapEnvelope(new XmlObjectToElementTransformer<>().apply(attributeQuery));
        String xmlString = XmlUtils.writeToString(soapEnvelope);

        URI uri = UriBuilder.fromPath(url).build();
        return client
                .target(uri.toASCIIString())
                .request()
                .post(Entity.entity(xmlString, MediaType.TEXT_XML));
    }
}
