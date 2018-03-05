package uk.gov.ida.matchingserviceadapter.repositories;

import org.opensaml.saml.common.xml.SAMLConstants;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.KeyDescriptor;
import org.opensaml.saml.saml2.metadata.RoleDescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.credential.UsageType;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.matchingserviceadapter.saml.errors.SamlTransformationErrorFactory;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.saml.core.validation.SamlValidationSpecificationFailure;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @deprecated
 * openSAML's implementation for fetching certificates when validating signatures should be used
 */
@Deprecated
public class CertificateExtractor {

    @Inject
    public CertificateExtractor() {
    }

    public List<Certificate> extractHubSigningCertificates(EntityDescriptor hubDescriptor) {
        SPSSODescriptor hubSsoDescriptor = hubDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        return getCertificates(hubSsoDescriptor, Certificate.KeyUse.Signing);
    }

    public List<Certificate> extractHubEncryptionCertificates(EntityDescriptor hubDescriptor) {
        SPSSODescriptor hubSsoDescriptor = hubDescriptor.getSPSSODescriptor(SAMLConstants.SAML20P_NS);
        return getCertificates(hubSsoDescriptor, Certificate.KeyUse.Encryption);
    }

    public List<Certificate> extractIdpSigningCertificates(EntityDescriptor idpDescriptor) {
        IDPSSODescriptor idpSSODescriptor = idpDescriptor.getIDPSSODescriptor(SAMLConstants.SAML20P_NS);
        return getCertificates(idpSSODescriptor, Certificate.KeyUse.Signing);
    }

    private List<Certificate> getCertificates(RoleDescriptor descriptor, Certificate.KeyUse keyType) {
        return descriptor.getKeyDescriptors().stream()
                .map(this::toCertificate)
                .filter(certificate -> certificate.getKeyUse() == keyType)
                .collect(Collectors.toList());
    }

    private Certificate toCertificate(KeyDescriptor keyDescriptor) {
        String entityId = null;
        if (!keyDescriptor.getKeyInfo().getKeyNames().isEmpty()) {
            entityId = keyDescriptor.getKeyInfo().getKeyNames().get(0).getValue();
        }
        return transformCertificate(entityId, keyDescriptor);
    }

    private Certificate transformCertificate(String entityId, KeyDescriptor keyDescriptor) {
        String x509Certificate = keyDescriptor.getKeyInfo().getX509Datas().get(0).getX509Certificates().get(0).getValue();
        final Certificate.KeyUse keyUse = transformUsageType(keyDescriptor.getUse());
        return new Certificate(entityId, x509Certificate, keyUse);
    }

    private Certificate.KeyUse transformUsageType(UsageType usageType) {

        switch (usageType) {
            case ENCRYPTION:
                return Certificate.KeyUse.Encryption;
            case SIGNING:
                return Certificate.KeyUse.Signing;
            case UNSPECIFIED:
                SamlValidationSpecificationFailure failure = SamlTransformationErrorFactory.unsupportedKey(usageType.toString());
                throw new SamlTransformationErrorException(failure.getErrorMessage(), failure.getLogLevel());
            default:
                throw new IllegalArgumentException("SamlObjectParser will have failed before reaching here.");
        }
    }
}

