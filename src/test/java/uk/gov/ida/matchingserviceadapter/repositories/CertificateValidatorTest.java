package uk.gov.ida.matchingserviceadapter.repositories;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.matchingserviceadapter.rest.configuration.verification.FixedCertificateChainValidator;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;

import java.security.cert.X509Certificate;
import java.util.Collections;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;

@RunWith(OpenSAMLMockitoRunner.class)
public class CertificateValidatorTest {
    @Mock
    private FixedCertificateChainValidator certificateChainValidator;
    @Mock
    private X509CertificateFactory certificateFactory;

    @Test
    public void shouldValidateX509Certificate() {
        String certificateAsString = "some encryption cert";
        Certificate certificate = aCertificate()
                .withCertificate(certificateAsString)
                .withKeyUse(Certificate.KeyUse.Encryption)
                .build();

        X509Certificate encryptionX509Certificate = mock(X509Certificate.class);
        when(certificateFactory.createCertificate(certificateAsString)).thenReturn(encryptionX509Certificate);

        CertificateValidator validator = new CertificateValidator(certificateFactory, certificateChainValidator);

        validator.validate(Collections.singletonList(certificate));

        verify(certificateChainValidator).validate(encryptionX509Certificate);
    }

}