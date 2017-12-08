package uk.gov.ida.matchingserviceadapter.validators;

import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.validation.messages.Messages;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectConfirmationDataValidator.CONFIRMATION_DATA_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectConfirmationValidator.WRONG_SUBJECT_CONFIRMATION_METHOD;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

@RunWith(OpenSAMLMockitoRunner.class)
public class SubjectConfirmationValidatorTest {

    private SubjectConfirmationValidator<SubjectConfirmation> validator;

    @Before
    public void setup() {
        validator = new SubjectConfirmationValidator<>(identity(), new DateTimeComparator(Duration.ZERO));
    }

    @Test
    public void shouldGenerateNoErrorsWhenSubjectConfirmationIsValid() {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
            aSubjectConfirmationData()
                .build()).build();

        Messages messages = validator.validate(subjectConfirmation, messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateErrorWhenSubjectConfirmationMethodIsNotBearer() throws Exception {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withMethod("anything-but-not-bearer").build();

        Messages messages = validator.validate(subjectConfirmation, messages());

        assertThat(messages.hasErrorLike(WRONG_SUBJECT_CONFIRMATION_METHOD)).isTrue();
    }

    @Test
    public void shouldValidateSubjectConfirmationData() throws Exception {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(null).build();

        Messages messages = validator.validate(subjectConfirmation, messages());

        assertThat(messages.hasErrorLike(CONFIRMATION_DATA_NOT_PRESENT)).isTrue();
    }

}