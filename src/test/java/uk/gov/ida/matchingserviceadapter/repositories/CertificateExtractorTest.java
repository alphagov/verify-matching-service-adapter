package uk.gov.ida.matchingserviceadapter.repositories;

import org.joda.time.DateTime;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.core.xml.io.MarshallingException;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.xmlsec.signature.KeyInfo;
import org.opensaml.xmlsec.signature.X509Certificate;
import org.opensaml.xmlsec.signature.X509Data;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.TestCertificateStrings;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.core.test.builders.metadata.EntityDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.IdpSsoDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.KeyDescriptorBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.KeyInfoBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.X509CertificateBuilder;
import uk.gov.ida.saml.core.test.builders.metadata.X509DataBuilder;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.saml.metadata.test.factories.metadata.EntityDescriptorFactory;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;

@RunWith(OpenSAMLMockitoRunner.class)
public class CertificateExtractorTest {

    @Test
    public void shouldExtractHubSigningCertificate() {
        CertificateExtractor extractor = new CertificateExtractor();

        EntityDescriptor entityDescriptor = new EntityDescriptorFactory().hubEntityDescriptor();

        List<Certificate> certificates = extractor.extractHubSigningCertificates(entityDescriptor);

        assertThat(certificates)
                .extracting("issuerId", "certificate", "keyUse")
                .contains(tuple("signing_one", TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT, Certificate.KeyUse.Signing),
                          tuple("signing_two", TestCertificateStrings.HUB_TEST_SECONDARY_PUBLIC_SIGNING_CERT, Certificate.KeyUse.Signing));
    }

    @Test
    public void shouldExtractHubEncryptionCertificate() {
        CertificateExtractor extractor = new CertificateExtractor();

        EntityDescriptor entityDescriptor = new EntityDescriptorFactory().hubEntityDescriptor();

        List<Certificate> certificates = extractor.extractHubEncryptionCertificates(entityDescriptor);

        assertThat(certificates)
                .extracting("issuerId", "certificate", "keyUse")
                .contains(tuple("encryption", TestCertificateStrings.HUB_TEST_PUBLIC_ENCRYPTION_CERT, Certificate.KeyUse.Encryption));
    }

    @Test
    public void shouldExtractIdpSigningCertificate() {
        CertificateExtractor extractor = new CertificateExtractor();

        EntityDescriptor entityDescriptor = new EntityDescriptorFactory().idpEntityDescriptor(TestEntityIds.STUB_IDP_ONE);

        List<Certificate> certificates = extractor.extractIdpSigningCertificates(entityDescriptor);

        assertThat(certificates)
                .extracting("issuerId", "certificate", "keyUse")
                .contains(tuple("signing_one", TestCertificateStrings.PUBLIC_SIGNING_CERTS.get(TestEntityIds.STUB_IDP_ONE), Certificate.KeyUse.Signing));
    }

    @Test
    public void shouldExtractIdpSigningCertificateWithNullKeyName() throws Exception {
        CertificateExtractor extractor = new CertificateExtractor();

        EntityDescriptor entityDescriptor = buildEntityDescriptor(null, "SIGNING");

        List<Certificate> certificates = extractor.extractIdpSigningCertificates(entityDescriptor);

        assertThat(certificates)
                .extracting("issuerId", "certificate", "keyUse")
                .contains(tuple(null, TestCertificateStrings.PUBLIC_SIGNING_CERTS.get(TestEntityIds.STUB_IDP_ONE), Certificate.KeyUse.Signing));
    }

    @Test(expected = SamlTransformationErrorException.class)
    public void shouldExtractIdpSigningCertificateWithUnspecifiedUsageType() throws Exception {
        CertificateExtractor extractor = new CertificateExtractor();

        EntityDescriptor entityDescriptor = buildEntityDescriptor("signing_one", "UNSPECIFIED");

        extractor.extractIdpSigningCertificates(entityDescriptor);
    }

    private EntityDescriptor buildEntityDescriptor(String keyName, String keyUse) throws MarshallingException, SignatureException {
        String certificate = TestCertificateStrings.PUBLIC_SIGNING_CERTS.get(TestEntityIds.STUB_IDP_ONE);
        X509Certificate x509Certificate = X509CertificateBuilder.aX509Certificate().withCert(certificate).build();
        X509Data build = X509DataBuilder.aX509Data().withX509Certificate(x509Certificate).build();
        KeyInfo signing_one = KeyInfoBuilder.aKeyInfo().withKeyName(keyName).withX509Data(build).build();
        KeyDescriptor keyDescriptor = KeyDescriptorBuilder.aKeyDescriptor().withKeyInfo(signing_one).withUse(keyUse).build();
        IDPSSODescriptor idpssoDescriptor = IdpSsoDescriptorBuilder.anIdpSsoDescriptor().addKeyDescriptor(keyDescriptor).withoutDefaultSigningKey().build();

        return EntityDescriptorBuilder.anEntityDescriptor()
                .withEntityId(TestEntityIds.STUB_IDP_ONE)
                .withIdpSsoDescriptor(idpssoDescriptor)
                .withValidUntil(DateTime.now().plusWeeks(2))
                .withSignature(null)
                .withoutSigning()
                .setAddDefaultSpServiceDescriptor(false)
                .build();
    }

}