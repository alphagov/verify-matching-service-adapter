package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.saml.saml2.core.AuthnStatement;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.RequiredValidator;

import java.util.function.Function;

import static uk.gov.ida.validation.messages.MessageImpl.fieldMessage;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;


public class AuthnStatementValidator<T> extends CompositeValidator<T> {

    public static final int AUTHN_INSTANT_VALIDITY_DURATION_IN_MINUTES = 5;

    public static final MessageImpl AUTHN_STATEMENT_NOT_PRESENT = globalMessage("authnStatement", "Authn statement not present");
    public static final MessageImpl AUTHN_INSTANT_IN_FUTURE = fieldMessage("authnStatement.authnInstant", "authnStatement.authnInstant.invalid", "Authn instant must not be in the future");
    public static final MessageImpl AUTHN_INSTANT_TOO_OLD = fieldMessage("authnStatement.authnInstant", "authnStatement.authnInstant.too.old", String.format("Authn instant must not be older than %s minutes", AUTHN_INSTANT_VALIDITY_DURATION_IN_MINUTES));

    public AuthnStatementValidator(Function<T, AuthnStatement> valueProvider, DateTimeComparator dateTimeComparator) {
        super(
            true,
            valueProvider,
            new RequiredValidator<>(AUTHN_STATEMENT_NOT_PRESENT),
            TimeRestrictionValidators.notInFutureValidator(dateTimeComparator, AuthnStatement::getAuthnInstant, AUTHN_INSTANT_IN_FUTURE),
            TimeRestrictionValidators.notInPastValidator(dateTimeComparator, as -> as.getAuthnInstant().plusMinutes(AUTHN_INSTANT_VALIDITY_DURATION_IN_MINUTES), AUTHN_INSTANT_TOO_OLD)
        );
    }

}
