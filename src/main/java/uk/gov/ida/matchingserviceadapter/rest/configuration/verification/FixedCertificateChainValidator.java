package uk.gov.ida.matchingserviceadapter.rest.configuration.verification;

import uk.gov.ida.common.shared.security.verification.CertificateChainValidator;
import uk.gov.ida.common.shared.security.verification.CertificateValidity;
import uk.gov.ida.common.shared.security.verification.exceptions.CertificateChainValidationException;

import javax.inject.Inject;
import java.security.KeyStore;
import java.security.cert.X509Certificate;

import static java.text.MessageFormat.format;

public class FixedCertificateChainValidator {
    private final KeyStore trustStore;
    private final CertificateChainValidator certificateChainValidator;

    @Inject
    public FixedCertificateChainValidator(
            KeyStore trustStore,
            CertificateChainValidator certificateChainValidator) {
        this.trustStore = trustStore;
        this.certificateChainValidator = certificateChainValidator;
    }

    public void validate(X509Certificate certificate) {
        CertificateValidity certificateValidity = certificateChainValidator.validate(certificate, trustStore);

        if (!certificateValidity.isValid()) {
            throw new CertificateChainValidationException(
                    format("Certificate is not valid: {0}", getDnForCertificate(certificate)),
                    certificateValidity.getException().orNull());
        }
    }

    private String getDnForCertificate(X509Certificate certificate) {
        if (certificate != null && certificate.getSubjectDN() != null) {
            return certificate.getSubjectDN().getName();
        }
        return "Unable to get DN";
    }
}
