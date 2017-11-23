package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.MessageImpl;
import org.beanplanet.validation.CompositeValidator;
import org.beanplanet.validation.FixedErrorValidator;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.x509.BasicX509Credential;
import uk.gov.ida.common.shared.security.Certificate;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateExtractor;
import uk.gov.ida.matchingserviceadapter.repositories.CertificateValidator;
import uk.gov.ida.matchingserviceadapter.repositories.MetadataCertificatesRepository;
import uk.gov.ida.saml.security.AssertionDecrypter;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static org.beanplanet.messages.domain.MessageImpl.fieldMessage;
import static org.beanplanet.messages.domain.MessageImpl.globalMessage;

public class EidasAttributeQueryValidator extends CompositeValidator<AttributeQuery> {

    public static final MessageImpl DEFAULT_ISSUER_REQUIRED_MESSAGE = fieldMessage("issuer", "issuer.empty", "Eidas Attribute Query issuer was not provided.");
    public static final MessageImpl DEFAULT_ISSUER_EMPTY_MESSAGE = fieldMessage("issuer", "issuer.empty", "Eidas Attribute Query issuer value was empty.");
    public static final MessageImpl DEFAULT_ENCRYPTED_ASSERTIONS_MISSING_MESSAGE = globalMessage("encrypted.assertions.cardinatlity", "There must be 1 EncryptedAssertion for eIDAS.");
    public static final MessageImpl DEFAULT_INVALID_SIGNATURE_MESSAGE = globalMessage("invalid.signature", "Eidas Attribute Query's signature was invalid.");
    public static final String IDENTITY_ASSERTION = "Identity";

    public EidasAttributeQueryValidator(MetadataResolver verifyMetadataResolver,
                                        MetadataResolver countryMetadataResolver,
                                        CertificateValidator verifyHubCertificateValidator,
                                        CertificateValidator countryMetadataCertificateValidator,
                                        CertificateExtractor certificateExtractor,
                                        X509CertificateFactory x509CertificateFactory,
                                        AssertionDecrypter assertionDecrypter) {
        super(
            false,
            new CompositeValidator<>(
                true,
                new IssuerValidator<>(DEFAULT_ISSUER_REQUIRED_MESSAGE, DEFAULT_ISSUER_EMPTY_MESSAGE, AttributeQuery::getIssuer),
                new SamlDigitalSignatureValidator<>(
                    DEFAULT_INVALID_SIGNATURE_MESSAGE,
                    attributeQuery -> new MetadataCertificatesRepository(verifyMetadataResolver, verifyHubCertificateValidator, certificateExtractor)
                        .getHubSigningCertificates(attributeQuery.getIssuer().getValue()).stream()
                        .map(Certificate::getCertificate)
                        .map(x509CertificateFactory::createCertificate)
                        .map(BasicX509Credential::new)
                        .collect(Collectors.toList()),
                    AttributeQuery::getIssuer,
                    SPSSODescriptor.DEFAULT_ELEMENT_NAME
                )
            ),
            new CompositeValidator<>(
                true,
                new FixedErrorValidator<>(aqr -> getEncryptedAssertions(aqr).size() != 1, DEFAULT_ENCRYPTED_ASSERTIONS_MISSING_MESSAGE),
                new CompositeValidator<>(
                    aqr -> assertionDecrypter.decryptAssertions(() -> getEncryptedAssertions(aqr)).get(0),
                    new EidasAttributeQueryAssertionValidator(
                        countryMetadataResolver,
                        countryMetadataCertificateValidator,
                        certificateExtractor,
                        x509CertificateFactory,
                        IDENTITY_ASSERTION)
                )
            )
        );
    }

    private static List<EncryptedAssertion> getEncryptedAssertions(AttributeQuery attributeQuery) {
        if (attributeQuery.getSubject() == null
            || attributeQuery.getSubject().getSubjectConfirmations().isEmpty()) {
            return Collections.emptyList();
        }

        return (List<EncryptedAssertion>) (List<?>) attributeQuery.getSubject()
            .getSubjectConfirmations()
            .stream()
            .flatMap(s -> s.getSubjectConfirmationData().getUnknownXMLObjects(EncryptedAssertion.DEFAULT_ELEMENT_NAME).stream())
            .collect(Collectors.toList());
    }

}
