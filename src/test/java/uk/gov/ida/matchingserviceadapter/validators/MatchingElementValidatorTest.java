package uk.gov.ida.matchingserviceadapter.validators;

import com.google.common.collect.ImmutableList;
import org.beanplanet.messages.domain.Message;
import org.beanplanet.messages.domain.MessageImpl;
import org.beanplanet.messages.domain.Messages;
import org.beanplanet.validation.PredicatedValidator;
import org.beanplanet.validation.Validator;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static uk.gov.ida.matchingserviceadapter.validators.MatchingElementValidator.NO_VALUE_MATCHING_FILTER;

public class MatchingElementValidatorTest {

    private Message message = MessageImpl.globalMessage("string.length", "string.length.too.short");

    private Validator<Collection<String>> validator;

    private Validator<String> lengthMoreThan3Validator = new PredicatedValidator<String>(null, identity(), message, (String s) -> s.length() > 3) {};

    @Before
    public void setup() {
        validator = new MatchingElementValidator<>(identity(), s -> s.contains("foo"), lengthMoreThan3Validator);
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

}