package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.security.SecurityException;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.saml.security.SignatureValidator;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.PredicatedValidator;

import javax.xml.namespace.QName;
import java.time.Duration;
import java.util.function.Predicate;

import static uk.gov.ida.matchingserviceadapter.validators.IssueInstantValidator.IssueInstantJodaDateTimeValidator;
import static uk.gov.ida.validation.messages.MessageImpl.fieldMessage;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;

public class AssertionValidator extends CompositeValidator<Assertion> {

    public AssertionValidator(
        final SignatureValidator signatureValidator,
        final DateTimeComparator dateTimeComparator,
        final String typeOfAssertion,
        final Duration ttl,
        final Duration clockDelta,
        final QName issuerRoleDescriptor) {
        super(
            false,
            new CompositeValidator<>(
                true,
                new IssuerValidator<>(
                    generateMissingIssuerMessage(typeOfAssertion),
                    generateEmptyIssuerMessage(typeOfAssertion),
                    Assertion::getIssuer
                ),
                new PredicatedValidator<>(generateInvalidSignatureMessage(typeOfAssertion), validateAssertionSignature(signatureValidator, issuerRoleDescriptor))
            ),
            new SubjectValidator<>(Assertion::getSubject, dateTimeComparator),
            IssueInstantJodaDateTimeValidator(
                fieldMessage("issueInstant","expired.message", "Issue Instant time-to-live has been exceeded"),
                fieldMessage("issueInstant","issue.instance.in.future", "Issue Instant is in the future"),
                Assertion::getIssueInstant,
                ttl,
                clockDelta
            )
        );
    }

    private static Predicate<Assertion> validateAssertionSignature(SignatureValidator signatureValidator, QName issuerRoleDescriptor) {
        return assertion -> {
            try {
                return signatureValidator.validate(assertion, assertion.getIssuer().getValue(), issuerRoleDescriptor);
            } catch (SecurityException | SignatureException e) {
                throw new RuntimeException(e);
            }
        };
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
}
