package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.Issuer;
import uk.gov.ida.matchingserviceadapter.repositories.MetadataRepository;
import uk.gov.ida.saml.security.AssertionDecrypter;

import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

public class EidasAttributesBasedAttributeQueryDiscriminator implements Predicate<AttributeQuery> {
    private final AssertionDecrypter assertionDecrypter;
    private final MetadataRepository metadataRepository;

    public EidasAttributesBasedAttributeQueryDiscriminator(final AssertionDecrypter assertionDecrypter,
                                                           final MetadataRepository metadataRepository) {
        this.assertionDecrypter = assertionDecrypter;
        this.metadataRepository = metadataRepository;
    }

    @Override
    public boolean test(AttributeQuery attributeQuery) {
        if (getNumberOfEncryptedAssertions(attributeQuery) != 1) {
            return false;
        }

        List<Assertion> assertions = decryptAssertions(attributeQuery);
        return isEidas(assertions);
    }

    private boolean isEidas(List<Assertion> assertions) {
        Issuer issuer = assertions.get(0).getIssuer();
        if (issuer == null || issuer.getValue() == null) {
            return false;
        }

        return metadataRepository.hasMetadataForEntity(issuer.getValue());
    }

    private int getNumberOfEncryptedAssertions(AttributeQuery attributeQuery) {
        return getEncryptedAssertions(attributeQuery).size();
    }

    @SuppressWarnings("unchecked")
    private List<EncryptedAssertion> getEncryptedAssertions(AttributeQuery attributeQuery) {
        if (attributeQuery.getSubject() == null
            || attributeQuery.getSubject().getSubjectConfirmations() == null) {
            return Collections.emptyList();
        }

        return (List<EncryptedAssertion>) (List<?>) attributeQuery.getSubject()
            .getSubjectConfirmations()
            .stream()
            .flatMap(s -> s.getSubjectConfirmationData().getUnknownXMLObjects(EncryptedAssertion.DEFAULT_ELEMENT_NAME).stream())
            .collect(Collectors.toList());
    }

    private List<Assertion> decryptAssertions(AttributeQuery attributeQuery) {
        return assertionDecrypter.decryptAssertions(() -> getEncryptedAssertions(attributeQuery));
    }
}
