package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.Message;
import org.beanplanet.messages.domain.Messages;
import org.beanplanet.validation.AbstractValueProvidedValidator;
import org.beanplanet.validation.CompositeValidator;
import org.beanplanet.validation.PredicatedValidator;
import org.beanplanet.validation.Validator;

import static org.beanplanet.messages.domain.MessageImpl.globalMessage;

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
