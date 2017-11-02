package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.saml.saml2.core.Issuer;
import uk.gov.ida.validation.messages.Message;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.NotEmptyValidator;
import uk.gov.ida.validation.validators.RequiredValidator;

import java.util.function.Function;

import static uk.gov.ida.validation.messages.MessageImpl.fieldMessage;

public class IssuerValidator<T> extends CompositeValidator<T> {

    public static final MessageImpl DEFAULT_REQUIRED_MESSAGE = fieldMessage("issuer", "issuer.empty", "The issuer was not provided.");
    public static final MessageImpl DEFAULT_EMPTY_VALUE_MESSAGE = fieldMessage("issuer.value", "issuer.value.empty", "The issuer value was not provided.");

    public IssuerValidator(Function<T, Issuer> valueProvider) {
        this(
            DEFAULT_REQUIRED_MESSAGE,
            DEFAULT_EMPTY_VALUE_MESSAGE,
            valueProvider
        );
    }

    public IssuerValidator(Message requiredMessage,
                           Message emptyValueMessage,
                           Function<T, Issuer> valueProvider) {
        super(
            true,
            valueProvider,
            new RequiredValidator<>(requiredMessage),
            new NotEmptyValidator<>(emptyValueMessage, Issuer::getValue)
        );
    }
}
