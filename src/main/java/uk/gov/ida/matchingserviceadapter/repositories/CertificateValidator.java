package uk.gov.ida.matchingserviceadapter.repositories;

import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.matchingserviceadapter.rest.configuration.verification.FixedCertificateChainValidator;

import javax.inject.Inject;
import java.util.List;

/**
 * @deprecated
 * openSAML's implementation for fetching certificates when validating signatures should be used
 */
@Deprecated
public class CertificateValidator {

    private final X509CertificateFactory certificateFactory;
    private final FixedCertificateChainValidator certificateChainValidator;

    @Inject
    public CertificateValidator(X509CertificateFactory certificateFactory, FixedCertificateChainValidator certificateChainValidator) {
        this.certificateFactory = certificateFactory;
        this.certificateChainValidator = certificateChainValidator;
    }

    public void validate(List<Certificate> certificates) {
        certificates.stream()
            .map(Certificate::getCertificate)
            .map(certificateFactory::createCertificate)
            .forEach(certificateChainValidator::validate);
    }
}
