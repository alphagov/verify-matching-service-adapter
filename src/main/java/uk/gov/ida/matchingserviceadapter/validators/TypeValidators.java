package uk.gov.ida.matchingserviceadapter.validators;


import uk.gov.ida.validation.messages.Message;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.AbstractValueProvidedValidator;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.PredicatedValidator;
import uk.gov.ida.validation.validators.Validator;

import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;

public class TypeValidators {

    public static <T> Validator<T> isInstanceOf(Class<?> clazz) {
        return new PredicatedValidator<T>(typeMismatchError(clazz), (T o) -> clazz.isInstance(o)) {};
    }

    public static <T,R> Validator<T> isValidatedInstanceOf(Class<R> clazz, Validator<R> validator) {
        return new CompositeValidator<>(
            true,
            isInstanceOf(clazz),
            new AbstractValueProvidedValidator<T>() {
                @Override
                protected Messages doValidate(T t, Messages messages) {
                    return validator.validate((R) t, messages);
                }
            }
        );
    }

    public static Message typeMismatchError(Class<?> clazz) {
        return globalMessage("type", String.format("Not an instance of %s", clazz.getName()));
    }
}
