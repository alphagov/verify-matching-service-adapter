package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.DateTime;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.validation.messages.Messages;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectConfirmationDataValidator.CONFIRMATION_DATA_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectConfirmationDataValidator.IN_RESPONSE_TO_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectConfirmationDataValidator.NOT_ON_OR_AFTER_INVALID;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectConfirmationDataValidator.NOT_ON_OR_AFTER_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectConfirmationDataValidator.RECIPIENT_NOT_PRESENT;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

@RunWith(OpenSAMLMockitoRunner.class)
public class SubjectConfirmationDataValidatorTest {

    private SubjectConfirmationDataValidator<SubjectConfirmationData> validator;

    @Before
    public void setup() {
        validator = new SubjectConfirmationDataValidator<>(identity(), new DateTimeComparator(Duration.ZERO));
    }

    @Test
    public void shouldGenerateNoErrorsWhenSubjectConfirmationDataIsValid() {
        SubjectConfirmationData subjectConfirmationData = aSubjectConfirmationData().build();

        Messages messages = validator.validate(subjectConfirmationData, messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateErrorWhenSubjectConfirmationDataMissing() throws Exception {
        Messages messages = validator.validate(null, messages());

        assertThat(messages.hasErrorLike(CONFIRMATION_DATA_NOT_PRESENT)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenSubjectConfirmationDataNotOnOrAfterIsMissing() throws Exception {
        SubjectConfirmationData subjectConfirmationData = aSubjectConfirmationData().withNotOnOrAfter(null).build();

        Messages messages = validator.validate(subjectConfirmationData, messages());

        assertThat(messages.hasErrorLike(NOT_ON_OR_AFTER_NOT_PRESENT)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenSubjectConfirmationDataNotOnOrAfterIsInThePast() throws Exception {
        SubjectConfirmationData subjectConfirmationData = aSubjectConfirmationData().withNotOnOrAfter(DateTime.now().minusMinutes(5)).withNotBefore(DateTime.now()).build();

        Messages messages = validator.validate(subjectConfirmationData, messages());

        assertThat(messages.hasErrorLike(NOT_ON_OR_AFTER_INVALID)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenSubjectConfirmationDataHasNoInResponseTo() throws Exception {
        SubjectConfirmationData subjectConfirmationData = aSubjectConfirmationData().withNotBefore(DateTime.now()).withInResponseTo(null).build();

        Messages messages = validator.validate(subjectConfirmationData, messages());

        assertThat(messages.hasErrorLike(IN_RESPONSE_TO_NOT_PRESENT)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenSubjectConfirmationDataHasNoRecipient() throws Exception {
        SubjectConfirmationData subjectConfirmationData = aSubjectConfirmationData().withNotBefore(DateTime.now()).withRecipient(null).build();

        Messages messages = validator.validate(subjectConfirmationData, messages());

        assertThat(messages.hasErrorLike(RECIPIENT_NOT_PRESENT)).isTrue();
    }

}