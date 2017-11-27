package uk.gov.ida.integrationtest;

import httpstub.HttpStubRule;
import io.dropwizard.testing.ConfigOverride;
import org.joda.time.DateTime;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.xmlsec.algorithm.DigestAlgorithm;
import org.opensaml.xmlsec.algorithm.SignatureAlgorithm;
import org.opensaml.xmlsec.algorithm.descriptors.DigestSHA256;
import org.opensaml.xmlsec.algorithm.descriptors.SignatureRSASHA1;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.Constants;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppRule;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.EntityDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.KeyDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.KeyInfoBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.SPSSODescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.X509CertificateBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.X509DataBuilder;
import uk.gov.ida.saml.metadata.test.factories.metadata.MetadataFactory;

import java.io.IOException;
import java.util.Collections;

import static com.google.common.base.Throwables.propagate;
import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.opensaml.saml.saml2.core.StatusCode.REQUESTER;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aSubjectWithAssertions;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.anAuthnStatementAssertion;
import static uk.gov.ida.integrationtest.helpers.RequestHelper.makeAttributeQueryRequest;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.matchers.SignableSAMLObjectBaseMatcher.signedBy;


public class MatchingServiceAdapterFailingMetadataAppRuleTest {
    private final String MATCHING_SERVICE_URI = "http://localhost:" + applicationRule.getLocalPort() + "/matching-service/POST";
    private static final String METADATA_PATH = "/uk/gov/ida/saml/metadata/federation";

    @ClassRule
    public static final HttpStubRule metadataServer = new HttpStubRule();

    @ClassRule
    public static final MatchingServiceAdapterAppRule applicationRule = new MatchingServiceAdapterAppRule(
        ConfigOverride.config("metadata.url", "http://localhost:" + metadataServer.getPort() + METADATA_PATH)
    );

    private final SignatureAlgorithm signatureAlgorithmForHub = new SignatureRSASHA1();
    private final DigestAlgorithm digestAlgorithmForHub = new DigestSHA256();

    @Test
    public void shouldReturnErrorResponseWhenAMatchRequestIsReceivedAndThereIsAProblemValidatingTheCertificateChainOfAHubCertificate() throws IOException, InterruptedException {
        String metadata = new MetadataFactory().metadata(Collections.singletonList(badHubEntityDescriptor()));
        metadataServer.register(METADATA_PATH, 200, Constants.APPLICATION_SAMLMETADATA_XML, metadata);

        AttributeQuery attributeQuery = AttributeQueryBuilder.anAttributeQuery()
                .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(aSubjectWithAssertions(asList(anAuthnStatementAssertion()), "request-id", "hub-entity-id"))
            .build();

        Response response = makeAttributeQueryRequest(MATCHING_SERVICE_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(REQUESTER);
        assertThat(response.getStatus().getStatusMessage().getMessage()).contains("Signature was not valid");
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }

    private EntityDescriptor badHubEntityDescriptor() {
        X509Certificate x509CertificateOne = X509CertificateBuilder.aX509Certificate().withCert(TestCertificateStrings.UNCHAINED_PUBLIC_CERT).build();
        X509Data x509DataOne = X509DataBuilder.aX509Data().withX509Certificate(x509CertificateOne).build();
        KeyInfo signingOne = KeyInfoBuilder.aKeyInfo().withKeyName("signing_one").withX509Data(x509DataOne).build();
        KeyDescriptor keyDescriptorOne = KeyDescriptorBuilder.aKeyDescriptor().withKeyInfo(signingOne).build();
        SPSSODescriptor spssoDescriptor = SPSSODescriptorBuilder.anSpServiceDescriptor()
                .addKeyDescriptor(keyDescriptorOne)
                .withoutDefaultSigningKey()
                .withoutDefaultEncryptionKey().build();
        try {
            return EntityDescriptorBuilder.anEntityDescriptor()
                    .withEntityId(HUB_ENTITY_ID)
                    .addSpServiceDescriptor(spssoDescriptor)
                    .withIdpSsoDescriptor(null)
                    .withValidUntil(DateTime.now().plusHours(1))
                    .withSignature(null)
                    .withoutSigning()
                    .build();
        } catch (MarshallingException | SignatureException e) {
            throw propagate(e);
        }
    }

}
