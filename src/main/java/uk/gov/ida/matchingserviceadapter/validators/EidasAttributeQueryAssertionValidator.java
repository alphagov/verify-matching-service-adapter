package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import uk.gov.ida.saml.security.SignatureValidator;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.FixedErrorValidator;

import java.time.Duration;

import static uk.gov.ida.matchingserviceadapter.validators.IssueInstantValidator.IssueInstantJodaDateTimeValidator;
import static uk.gov.ida.validation.messages.MessageImpl.fieldMessage;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;

public class EidasAttributeQueryAssertionValidator extends CompositeValidator<Assertion> {

    public EidasAttributeQueryAssertionValidator(final SignatureValidator signatureValidator,
                                                 final DateTimeComparator dateTimeComparator,
                                                 final String typeOfAssertion,
                                                 final String hubConnectorEntityId,
                                                 final Duration ttl,
                                                 final Duration clockDelta) {
        super(
            false,
            new CompositeValidator<>(
                true,
                new IssuerValidator<>(
                    generateMissingIssuerMessage(typeOfAssertion),
                    generateEmptyIssuerMessage(typeOfAssertion),
                    Assertion::getIssuer
                ),

                //This needs to change. First of all, as Chris points out we should use opensaml
                //Secondly, the metadataResolver already validates the certificate for us (See DropwizardMetadataResolverFactory.java)!
                //There's no need to do it again ourselves, and in fact we can't now that we won't have a country trust store.
                new SamlDigitalSignatureValidator<>(
                    generateInvalidSignatureMessage(typeOfAssertion),
                    signatureValidator,
                    Assertion::getIssuer,
                    IDPSSODescriptor.DEFAULT_ELEMENT_NAME
                )
            ),
            new SubjectValidator<>(Assertion::getSubject, dateTimeComparator),
            IssueInstantJodaDateTimeValidator(
                globalMessage("expired.message", "Issue Instant time-to-live has been exceeded"),
                globalMessage("issue.instance.in.future", "Issue Instant is in the future"),
                Assertion::getIssueInstant,
                ttl,
                clockDelta
            ),
            new CompositeValidator<>(
                true,
                new FixedErrorValidator<>(a -> a.getAuthnStatements().size() != 1, generateWrongNumberOfAuthnStatementsMessage(typeOfAssertion)),
                new AuthnStatementValidator<>(a -> a.getAuthnStatements().get(0), dateTimeComparator)
            ),
            new ConditionsValidator<>(Assertion::getConditions, hubConnectorEntityId, dateTimeComparator),
            new CompositeValidator<>(
                true,
                new FixedErrorValidator<>(a -> a.getAttributeStatements().size() != 1 , generateWrongNumberOfAttributeStatementsMessage(typeOfAssertion)),
                new AttributeStatementValidator<>(a -> a.getAttributeStatements().get(0))
            )
        );
    }

    public static MessageImpl generateEmptyIssuerMessage(final String typeOfAssertion) {
        return fieldMessage("issuer.value", "issuer.value.empty", typeOfAssertion + " Assertion's issuer was empty.");
    }

    public static MessageImpl generateMissingIssuerMessage(final String typeOfAssertion) {
        return fieldMessage("issuer", "issuer.empty", typeOfAssertion + " Assertion's issuer was not provided.");
    }

    public static MessageImpl generateInvalidSignatureMessage(final String typeOfAssertion) {
        return globalMessage("invalid.signature", typeOfAssertion + " Assertion's signature was invalid.");
    }

    public static MessageImpl generateWrongNumberOfAuthnStatementsMessage(final String typeOfAssertion) {
        return fieldMessage("authnStatements", "authnStatements.wrong.number", typeOfAssertion + " Assertion had wrong number of authn statements.");
    }

    public static MessageImpl generateWrongNumberOfAttributeStatementsMessage(final String typeOfAssertion) {
        return fieldMessage("attributeStatements", "attributeStatements.wrong.number", typeOfAssertion + " Assertion had wrong number of attribute statements.");
    }
}
