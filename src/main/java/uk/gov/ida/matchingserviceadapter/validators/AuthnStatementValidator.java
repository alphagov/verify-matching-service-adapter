package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.MessageImpl;
import org.beanplanet.validation.CompositeValidator;
import org.beanplanet.validation.RequiredValidator;
import org.opensaml.saml.saml2.core.AuthnStatement;

import java.util.function.Function;

import static org.beanplanet.messages.domain.MessageImpl.fieldMessage;
import static org.beanplanet.messages.domain.MessageImpl.globalMessage;

public class AuthnStatementValidator<T> extends CompositeValidator<T> {

    public static final int AUTHN_INSTANT_VALIDITY_DURATION_IN_MINUTES = 5;

    public static final MessageImpl AUTHN_STATEMENT_NOT_PRESENT = globalMessage("authnStatement", "Authn statement not present");
    public static final MessageImpl AUTHN_INSTANT_NOT_PRESENT = fieldMessage("authnStatement.authnInstant", "authnStatement.authnInstant.absent", "Authn instant must be present");
    public static final MessageImpl AUTHN_INSTANT_IN_FUTURE = fieldMessage("authnStatement.authnInstant", "authnStatement.authnInstant.invalid", "Authn instant must not be in the future");
    public static final MessageImpl AUTHN_INSTANT_TOO_OLD = fieldMessage("authnStatement.authnInstant", "authnStatement.authnInstant.too.old", String.format("Authn instant must not be older than %s minutes", AUTHN_INSTANT_VALIDITY_DURATION_IN_MINUTES));

    public AuthnStatementValidator(Function<T, AuthnStatement> valueProvider, DateTimeComparator dateTimeComparator) {
        super(
            true,
            valueProvider,
            new RequiredValidator<>(AUTHN_STATEMENT_NOT_PRESENT),
            new RequiredValidator<>(AUTHN_INSTANT_NOT_PRESENT, AuthnStatement::getAuthnInstant),
            TimeRestrictionValidators.notInFutureValidator(dateTimeComparator, AuthnStatement::getAuthnInstant, AUTHN_INSTANT_IN_FUTURE),
            TimeRestrictionValidators.notInPastValidator(dateTimeComparator, as -> as.getAuthnInstant().plusMinutes(AUTHN_INSTANT_VALIDITY_DURATION_IN_MINUTES), AUTHN_INSTANT_TOO_OLD)
        );
    }

}
