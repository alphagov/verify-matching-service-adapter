package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.Messages;
import org.joda.time.Duration;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.test.builders.NameIdBuilder;

import static java.util.function.Function.identity;
import static org.assertj.core.api.Assertions.assertThat;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectConfirmationDataValidator.CONFIRMATION_DATA_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectConfirmationDataValidator.IN_RESPONSE_TO_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectConfirmationDataValidator.NOT_ON_OR_AFTER_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectConfirmationDataValidator.RECIPIENT_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectConfirmationValidator.WRONG_SUBJECT_CONFIRMATION_METHOD;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectValidator.NAME_ID_IN_WRONG_FORMAT;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectValidator.NAME_ID_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectValidator.SUBJECT_NOT_PRESENT;
import static uk.gov.ida.matchingserviceadapter.validators.SubjectValidator.WRONG_NUMBER_OF_SUBJECT_CONFIRMATIONS;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

@RunWith(MockitoJUnitRunner.class)
public class SubjectValidatorTest {
    private static final String IN_RESPONSE_TO = "_some-request-id";
    private SubjectValidator<Subject> subjectValidator;

    @Before
    public void setUp() {
        IdaSamlBootstrap.bootstrap();
        subjectValidator = new SubjectValidator(identity(), new DateTimeComparator(Duration.ZERO));
    }

    @Test
    public void shouldGenerateNoErrors() {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
            aSubjectConfirmationData()
                .withInResponseTo(IN_RESPONSE_TO)
                .build()).build();
        Subject subject = aSubject()
            .withSubjectConfirmation(subjectConfirmation)
            .build();

        Messages messages = subjectValidator.validate(subject, messages());

        assertThat(messages.hasErrors()).isFalse();
    }

    @Test
    public void shouldGenerateErrorWhenSubjectIsMissing() throws Exception {
        Messages messages = subjectValidator.validate(null, messages());

        assertThat(messages.hasErrorLike(SUBJECT_NOT_PRESENT)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenNameIdIsMissing() throws Exception {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
            aSubjectConfirmationData()
                .withInResponseTo(IN_RESPONSE_TO)
                .build()).build();
        Subject subject = aSubject()
            .withSubjectConfirmation(subjectConfirmation)
            .withNameId(null)
            .build();

        Messages messages = subjectValidator.validate(subject, messages());

        assertThat(messages.hasErrorLike(NAME_ID_NOT_PRESENT)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenNameIdFormatIsIncorrect() throws Exception {
        String incorrectFormat = "An incorrect format";

        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
            aSubjectConfirmationData()
                .withInResponseTo(IN_RESPONSE_TO)
                .build()).build();
        Subject subject = aSubject()
            .withSubjectConfirmation(subjectConfirmation)
            .withNameId(NameIdBuilder.aNameId().withFormat(incorrectFormat).build())
            .build();

        Messages messages = subjectValidator.validate(subject, messages());

        assertThat(messages.hasErrorLike(NAME_ID_IN_WRONG_FORMAT)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenMultipleSubjectConfirmations() throws Exception {
        Subject subject = aSubject()
            .withSubjectConfirmation(aSubjectConfirmation().build())
            .withSubjectConfirmation(aSubjectConfirmation().build())
            .build();

        Messages messages = subjectValidator.validate(subject, messages());

        assertThat(messages.hasErrorLike(WRONG_NUMBER_OF_SUBJECT_CONFIRMATIONS)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenSubjectConfirmationMethodIsNotBearer() throws Exception {
        Subject subject = aSubject()
                .withSubjectConfirmation(aSubjectConfirmation().withMethod("anything-but-not-bearer").build())
                .build();

        Messages messages = subjectValidator.validate(subject, messages());

        assertThat(messages.hasErrorLike(WRONG_SUBJECT_CONFIRMATION_METHOD)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenSubjectConfirmationDataMissing() throws Exception {
        Subject subject = aSubject()
                .withSubjectConfirmation(aSubjectConfirmation().withSubjectConfirmationData(null).build())
                .build();

        Messages messages = subjectValidator.validate(subject, messages());

        assertThat(messages.hasErrorLike(CONFIRMATION_DATA_NOT_PRESENT)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenSubjectConfirmationDataNotOnOrAfterIsMissing() throws Exception {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
                aSubjectConfirmationData().withNotOnOrAfter(null).build()).build();
        Subject subject = aSubject()
                .withSubjectConfirmation(subjectConfirmation)
                .build();

        Messages messages = subjectValidator.validate(subject, messages());

        assertThat(messages.hasErrorLike(NOT_ON_OR_AFTER_NOT_PRESENT)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenSubjectConfirmationDataHasNoInResponseTo() throws Exception {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
                aSubjectConfirmationData()
                        .withInResponseTo(null)
                        .build()).build();
        Subject subject = aSubject()
                .withSubjectConfirmation(subjectConfirmation)
                .build();

        Messages messages = subjectValidator.validate(subject, messages());

        assertThat(messages.hasErrorLike(IN_RESPONSE_TO_NOT_PRESENT)).isTrue();
    }

    @Test
    public void shouldGenerateErrorWhenSubjectConfirmationDataHasNoRecipient() throws Exception {
        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
                aSubjectConfirmationData()
                        .withInResponseTo(IN_RESPONSE_TO)
                        .withRecipient(null)
                        .build()).build();
        Subject subject = aSubject()
                .withSubjectConfirmation(subjectConfirmation)
                .build();

        Messages messages = subjectValidator.validate(subject, messages());

        assertThat(messages.hasErrorLike(RECIPIENT_NOT_PRESENT)).isTrue();
    }
}