package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.MessageImpl;
import org.beanplanet.messages.domain.Messages;
import org.beanplanet.validation.AbstractValueProvidedValidator;
import org.beanplanet.validation.Validator;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static uk.gov.ida.matchingserviceadapter.validators.TypeValidators.typeMismatchError;

public class TypeValidatorsTest {

    public static class A {}
    public static class B extends A {}
    public static class C extends A {}
    public static class D extends B {}

    @Test
    public void shouldGenerateNoErrorWhenTypesMatchExactly() {
        Validator<A> validator = TypeValidators.isInstanceOf(A.class);

        Messages messages = validator.validate(new A(), messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateNoErrorWhenTypesMatchUpToInheritance() throws Exception {
        Validator<A> validator = TypeValidators.isInstanceOf(A.class);

        Messages messages = validator.validate(new B(), messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateErrorWhenTypesDontMatch() {
        Validator<A> validator = TypeValidators.isInstanceOf(B.class);

        Messages messages = validator.validate(new C(), messages());

        assertThat(messages.hasErrorLike(typeMismatchError(B.class))).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenTypesMatchButProvidedValidationFails() {
        MessageImpl errorMessage = MessageImpl.globalMessage("error", "error.code");
        Validator<B> bValidator = new AbstractValueProvidedValidator<B>() {
            @Override
            protected Messages doValidate(B b, Messages messages) {
                return messages.addError(errorMessage);
            }
        };
        Validator<A> validator = TypeValidators.isValidatedInstanceOf(B.class, bValidator);

        Messages messages = validator.validate(new D(), messages());

        assertThat(messages.hasErrorLike(errorMessage)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenTypesMatchAndProvidedValidationSucceeds() {
        Validator<B> bValidator = new AbstractValueProvidedValidator<B>() {
            @Override
            protected Messages doValidate(B b, Messages messages) {
                return messages;
            }
        };
        Validator<A> validator = TypeValidators.isValidatedInstanceOf(B.class, bValidator);

        Messages messages = validator.validate(new D(), messages());

        assertThat(messages.hasErrors()).isFalse();
    }
}