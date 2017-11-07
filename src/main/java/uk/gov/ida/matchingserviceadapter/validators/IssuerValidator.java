package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.Message;
import org.beanplanet.messages.domain.MessageImpl;
import org.beanplanet.validation.CompositeValidator;
import org.beanplanet.validation.NotEmptyValidator;
import org.beanplanet.validation.RequiredValidator;
import org.opensaml.saml.saml2.core.Issuer;

import java.util.function.Function;

import static org.beanplanet.messages.domain.MessageImpl.fieldMessage;

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
