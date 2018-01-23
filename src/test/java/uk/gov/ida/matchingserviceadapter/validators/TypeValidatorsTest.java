package uk.gov.ida.matchingserviceadapter.validators;

import org.junit.Test;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.AbstractValueProvidedValidator;
import uk.gov.ida.validation.validators.Validator;

import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.validators.TypeValidators.typeMismatchError;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class TypeValidatorsTest {

    public static class A {}
    public static class B extends A {}
    public static class C extends A {}
    public static class D extends B {}

    @Test
    public void shouldGenerateNoErrorsWhenTypesMatchExactly() {
        Validator<A> validator = TypeValidators.isInstanceOf(A.class);

        Messages messages = validator.validate(new A(), messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateNoErrorsWhenTypesMatchUpToInheritance() throws Exception {
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
    public void shouldGenerateNoErrorsWhenTypesMatchAndProvidedValidationSucceeds() {
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