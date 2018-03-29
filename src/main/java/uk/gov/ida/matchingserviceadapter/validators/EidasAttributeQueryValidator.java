package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.SecurityException;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.matchingserviceadapter.saml.HubAssertionExtractor;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.SignatureValidator;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.FixedErrorValidator;
import uk.gov.ida.validation.validators.PredicatedValidator;

import java.time.Duration;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static uk.gov.ida.validation.messages.MessageImpl.fieldMessage;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;

public class EidasAttributeQueryValidator extends CompositeValidator<AttributeQuery> {

    public static final MessageImpl DEFAULT_ISSUER_REQUIRED_MESSAGE = fieldMessage("issuer", "issuer.empty", "Eidas Attribute Query issuer was not provided.");
    public static final MessageImpl DEFAULT_ISSUER_EMPTY_MESSAGE = fieldMessage("issuer", "issuer.empty", "Eidas Attribute Query issuer value was empty.");
    public static final MessageImpl DEFAULT_ENCRYPTED_ASSERTIONS_MISSING_MESSAGE = globalMessage("encrypted.assertions.cardinality", "There must be at least one EncryptedAssertion for eIDAS.");
    public static final MessageImpl DEFAULT_TOO_MANY_CYCLE_3_ASSERTIONS_MESSAGE = globalMessage("encrypted.assertions.cardinality", "There must not be more than one Cycle 3 assertion.");
    public static final MessageImpl DEFAULT_INVALID_SIGNATURE_MESSAGE = globalMessage("invalid.signature", "Eidas Attribute Query's signature was invalid.");
    public static final String IDENTITY_ASSERTION = "Identity";

    public EidasAttributeQueryValidator(SignatureValidator verifySignatureValidator,
                                        SignatureValidator countrySignatureValidator,
                                        DateTimeComparator dateTimeComparator,
                                        AssertionDecrypter assertionDecrypter,
                                        HubAssertionExtractor hubAssertionExtractor,
                                        final String hubConnectorEntityId) {
        super(
            false,
            new CompositeValidator<>(
                true,
                new IssuerValidator<>(DEFAULT_ISSUER_REQUIRED_MESSAGE, DEFAULT_ISSUER_EMPTY_MESSAGE, AttributeQuery::getIssuer),
                new PredicatedValidator<>(DEFAULT_INVALID_SIGNATURE_MESSAGE, validateAttributeQuerySignature(verifySignatureValidator))
            ),
            new CompositeValidator<>(
                false,
                (AttributeQuery aqr) -> assertionDecrypter.decryptAssertions(() -> getEncryptedAssertions(aqr)),
                new CompositeValidator<>(
                    true,
                    hubAssertionExtractor::getNonHubAssertions,
                    new FixedErrorValidator<>(assertions -> assertions.size() != 1, DEFAULT_ENCRYPTED_ASSERTIONS_MISSING_MESSAGE),
                    new CompositeValidator<>(
                        list -> list.get(0),
                        new EidasAttributeQueryAssertionValidator(
                            countrySignatureValidator,
                            dateTimeComparator,
                            IDENTITY_ASSERTION,
                            hubConnectorEntityId,
                            Duration.parse("PT20M"),
                            Duration.parse("PT1M"),
                            IDPSSODescriptor.DEFAULT_ELEMENT_NAME
                        ),
                        new CompositeValidator<>(
                                true,
                                new FixedErrorValidator<>(a -> a.getAuthnStatements().size() != 1, generateWrongNumberOfAuthnStatementsMessage(IDENTITY_ASSERTION)),
                                new AuthnStatementValidator<>(a -> a.getAuthnStatements().get(0), dateTimeComparator)
                        ),
                        new ConditionsValidator<>(Assertion::getConditions, hubConnectorEntityId, dateTimeComparator),
                        new CompositeValidator<>(
                            true,
                            new FixedErrorValidator<>(a -> a.getAttributeStatements().size() != 1 , generateWrongNumberOfAttributeStatementsMessage(IDENTITY_ASSERTION)),
                            new AttributeStatementValidator<>(a -> a.getAttributeStatements().get(0))
                        )
                    )
                ),
                new CompositeValidator<>(
                    assertions -> (assertions.size() > 1),
                    true,
                    hubAssertionExtractor::getHubAssertions,
                    new FixedErrorValidator<>(hubAssertions -> hubAssertions.size() > 1, DEFAULT_TOO_MANY_CYCLE_3_ASSERTIONS_MESSAGE),
                    new CompositeValidator<>(
                        list -> list.get(0),
                        new EidasAttributeQueryAssertionValidator(
                            verifySignatureValidator,
                            dateTimeComparator,
                            "Cycle 3",
                            hubConnectorEntityId,
                            Duration.parse("PT20M"),
                            Duration.parse("PT1M"),
                            SPSSODescriptor.DEFAULT_ELEMENT_NAME
                        )
                    )
                )
            )
        );
    }

    private static Predicate<AttributeQuery> validateAttributeQuerySignature(SignatureValidator verifySignatureValidator) {
        return attributeQuery -> {
            try {
                return verifySignatureValidator.validate(attributeQuery, attributeQuery.getIssuer().getValue(), SPSSODescriptor.DEFAULT_ELEMENT_NAME);
            } catch (SecurityException | SignatureException e) {
                throw new RuntimeException(e);
            }
        };
    }

    public static MessageImpl generateWrongNumberOfAuthnStatementsMessage(final String typeOfAssertion) {
        return fieldMessage("authnStatements", "authnStatements.wrong.number", typeOfAssertion + " Assertion had wrong number of authn statements.");
    }

    public static MessageImpl generateWrongNumberOfAttributeStatementsMessage(final String typeOfAssertion) {
        return fieldMessage("attributeStatements", "attributeStatements.wrong.number", typeOfAssertion + " Assertion had wrong number of attribute statements.");
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
