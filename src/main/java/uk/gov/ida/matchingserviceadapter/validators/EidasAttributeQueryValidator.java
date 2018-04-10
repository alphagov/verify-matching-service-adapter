package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.SignatureValidator;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.FixedErrorValidator;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static uk.gov.ida.validation.messages.MessageImpl.fieldMessage;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;

public class EidasAttributeQueryValidator extends CompositeValidator<AttributeQuery> {

    public static final MessageImpl DEFAULT_ISSUER_REQUIRED_MESSAGE = fieldMessage("issuer", "issuer.empty", "Eidas Attribute Query issuer was not provided.");
    public static final MessageImpl DEFAULT_ISSUER_EMPTY_MESSAGE = fieldMessage("issuer", "issuer.empty", "Eidas Attribute Query issuer value was empty.");
    public static final MessageImpl DEFAULT_ENCRYPTED_ASSERTIONS_MISSING_MESSAGE = globalMessage("encrypted.assertions.cardinality", "There must be 1 EncryptedAssertion for eIDAS.");
    public static final MessageImpl DEFAULT_INVALID_SIGNATURE_MESSAGE = globalMessage("invalid.signature", "Eidas Attribute Query's signature was invalid.");
    public static final String IDENTITY_ASSERTION = "Identity";

    public EidasAttributeQueryValidator(SignatureValidator verifySignatureValidator,
                                        SignatureValidator countrySignatureValidator,
                                        DateTimeComparator dateTimeComparator,
                                        AssertionDecrypter assertionDecrypter,
                                        final String hubConnectorEntityId) {
        super(
            false,
            new CompositeValidator<>(
                true,
                new IssuerValidator<>(DEFAULT_ISSUER_REQUIRED_MESSAGE, DEFAULT_ISSUER_EMPTY_MESSAGE, AttributeQuery::getIssuer),
                new SamlDigitalSignatureValidator<>(
                        DEFAULT_INVALID_SIGNATURE_MESSAGE,
                        verifySignatureValidator,
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
                            countrySignatureValidator,
                            dateTimeComparator,
                        IDENTITY_ASSERTION,
                        hubConnectorEntityId,
                        Duration.parse("PT20M"),
                        Duration.parse("PT1M"))
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
