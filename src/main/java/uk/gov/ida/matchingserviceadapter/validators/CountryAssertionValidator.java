package uk.gov.ida.matchingserviceadapter.validators;

import java.time.Duration;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;

import uk.gov.ida.saml.security.SignatureValidator;
import uk.gov.ida.validation.messages.Message;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.FixedErrorValidator;

import static uk.gov.ida.validation.messages.MessageImpl.fieldMessage;

public class CountryAssertionValidator extends CompositeValidator<Assertion> {
    public static final String IDENTITY_ASSERTION_TYPE_NAME = "Identity";
    private static final Duration MAXIMUM_AGE_OF_ASSERTION = Duration.ofMinutes(20);
    private static final Duration CLOCK_DELTA = Duration.ofMinutes(1);

    public CountryAssertionValidator(
        SignatureValidator countrySignatureValidator,
        DateTimeComparator dateTimeComparator,
        String hubConnectorEntityId) {
        super(
            true,
            new AssertionValidator(
                countrySignatureValidator,
                dateTimeComparator,
                IDENTITY_ASSERTION_TYPE_NAME,
                MAXIMUM_AGE_OF_ASSERTION,
                CLOCK_DELTA,
                IDPSSODescriptor.DEFAULT_ELEMENT_NAME
            ),
            new CompositeValidator<>(
                true,
                new FixedErrorValidator<>(a -> a.getAuthnStatements().size() != 1, generateWrongNumberOfAuthnStatementsMessage(IDENTITY_ASSERTION_TYPE_NAME)),
                new AuthnStatementValidator<>(a -> a.getAuthnStatements().get(0), dateTimeComparator)
            ),
            new CompositeValidator<>(
                true,
                new FixedErrorValidator<>(a -> a.getAttributeStatements().size() != 1 , generateWrongNumberOfAttributeStatementsMessage(IDENTITY_ASSERTION_TYPE_NAME)),
                new AttributeStatementValidator<>(a -> a.getAttributeStatements().get(0))
            ),
            new ConditionsValidator<>(Assertion::getConditions, hubConnectorEntityId, dateTimeComparator)
        );
    }

    public static Message generateWrongNumberOfAuthnStatementsMessage(final String typeOfAssertion) {
        return fieldMessage("authnStatements", "authnStatements.wrong.number", typeOfAssertion + " Assertion had wrong number of authn statements.");
    }

    public static Message generateWrongNumberOfAttributeStatementsMessage(final String typeOfAssertion) {
        return fieldMessage("attributeStatements", "attributeStatements.wrong.number", typeOfAssertion + " Assertion had wrong number of attribute statements.");
    }
}
