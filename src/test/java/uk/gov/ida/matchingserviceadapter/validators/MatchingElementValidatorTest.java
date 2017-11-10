package uk.gov.ida.matchingserviceadapter.validators;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Test;
import uk.gov.ida.validation.messages.Message;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.PredicatedValidator;
import uk.gov.ida.validation.validators.Validator;

import java.util.Collection;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.validators.MatchingElementValidator.NO_VALUE_MATCHING_FILTER;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class MatchingElementValidatorTest {

    private Message message = MessageImpl.globalMessage("string.length", "string.length.too.short");

    private Validator<Collection<String>> validator;

    private Validator<String> lengthMoreThan3Validator = new PredicatedValidator<String>(null, identity(), message, (String s) -> s.length() > 3) {
    };

    @Before
    public void setup() {
        validator = MatchingElementValidator.failOnMatchError(identity(), s -> s.contains("foo"), lengthMoreThan3Validator);
    }

    @Test
    public void shouldGenerateNoErrorsIfElementMatchesFilterAndIsValid() {
        Messages messages = validator.validate(ImmutableList.of("bar", "foofoo"), messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateErrorIfNoElementMatchesFilter() {
        Messages messages = validator.validate(ImmutableList.of("bar"), messages());

        assertThat(messages.hasErrorLike(NO_VALUE_MATCHING_FILTER)).isTrue();
    }

    @Test
    public void shouldGenerateErrorIfElementMatchesFilterButFailsValidation() {
        Messages messages = validator.validate(ImmutableList.of("foo"), messages());

        assertThat(messages.hasErrorLike(message)).isTrue();
    }

    @Test
    public void shouldGenerateNoErrorsIfErrorOnMatchFailureAndElementMatchesFilterAndIsValid() {
        validator = MatchingElementValidator.succeedOnMatchError(identity(), s -> s.contains("foo"), lengthMoreThan3Validator);

        Messages messages = validator.validate(ImmutableList.of("bar", "foofoo"), messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateNoErrorsIfNotErrorOnMatchFailure() {
        validator = MatchingElementValidator.succeedOnMatchError(identity(), s -> s.contains("foo"), lengthMoreThan3Validator);

        Messages messages = validator.validate(ImmutableList.of("bar"), messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateErrorIfErrorOnMatchFailureAndElementMatchesFilterButFailsValidation() {
        validator = MatchingElementValidator.succeedOnMatchError(identity(), s -> s.contains("foo"), lengthMoreThan3Validator);

        Messages messages = validator.validate(ImmutableList.of("foo"), messages());

        assertThat(messages.hasErrorLike(message)).isTrue();
    }

}