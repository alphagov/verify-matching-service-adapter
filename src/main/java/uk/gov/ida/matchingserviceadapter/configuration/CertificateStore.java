package uk.gov.ida.matchingserviceadapter.configuration;

import uk.gov.ida.common.shared.security.Certificate;

import java.util.List;

public class CertificateStore {

    private final List<Certificate> publicEncryptionCertificates;
    private final List<Certificate> publicSigningCertificates;

    public CertificateStore(
            List<Certificate> publicEncryptionKeyConfigurations,
            List<Certificate> publicSigningKeyConfiguration) {

        this.publicEncryptionCertificates = publicEncryptionKeyConfigurations;
        this.publicSigningCertificates = publicSigningKeyConfiguration;
    }

    public List<Certificate> getEncryptionCertificates() {
        return publicEncryptionCertificates;
    }

    public List<Certificate> getSigningCertificates() {
        return publicSigningCertificates;
    }

}
