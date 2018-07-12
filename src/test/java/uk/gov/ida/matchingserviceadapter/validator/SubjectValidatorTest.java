package uk.gov.ida.matchingserviceadapter.validator;

import com.google.common.collect.ImmutableList;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;
import uk.gov.ida.matchingserviceadapter.validators.SubjectValidator;
import uk.gov.ida.matchingserviceadapter.validators.TimeRestrictionValidator;
import uk.gov.ida.saml.core.IdaSamlBootstrap;

import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

@RunWith(MockitoJUnitRunner.class)
public class SubjectValidatorTest {
    private static final String IN_RESPONSE_TO = "_some-request-id";
    private SubjectValidator subjectValidator;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Mock
    private TimeRestrictionValidator timeRestrictionValidator;

    @Before
    public void setUp() {
        IdaSamlBootstrap.bootstrap();
        subjectValidator = new SubjectValidator(timeRestrictionValidator);
    }

    @Test
    public void shouldThrowExceptionWhenSubjectIsMissing() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Subject is missing from the assertion.");

        subjectValidator.validate(null, IN_RESPONSE_TO);
    }


    @Test
    public void shouldThrowExceptionWhenSubjectConfirmationDataMissing() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Subject confirmation data is missing from the assertion.");

        Subject subject = aSubject()
                .withSubjectConfirmation(aSubjectConfirmation().withSubjectConfirmationData(null).build())
                .build();

        subjectValidator.validate(subject, IN_RESPONSE_TO);
    }

    @Test
    public void shouldThrowExceptionWhenSubjectConfirmationDataNotOnOrAfterIsMissing() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Subject confirmation data must contain 'NotOnOrAfter'.");

        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
                aSubjectConfirmationData().withNotOnOrAfter(null).build()).build();
        Subject subject = aSubject()
                .withSubjectConfirmation(subjectConfirmation)
                .build();

        subjectValidator.validate(subject, IN_RESPONSE_TO);
    }

    @Test
    public void shouldThrowExceptionWhenSubjectConfirmationDataHasNoInResponseTo() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Subject confirmation data must contain 'InResponseTo'.");

        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
                aSubjectConfirmationData()
                        .withInResponseTo(null)
                        .build()).build();
        Subject subject = aSubject()
                .withSubjectConfirmation(subjectConfirmation)
                .build();

        subjectValidator.validate(subject, IN_RESPONSE_TO);
    }



    @Test
    public void shouldThrowExceptionWhenNameIdIsMissing() throws Exception {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("NameID is missing from the subject of the assertion.");

        SubjectConfirmation subjectConfirmation = aSubjectConfirmation().withSubjectConfirmationData(
                aSubjectConfirmationData()
                        .withInResponseTo(IN_RESPONSE_TO)
                        .build()).build();
        Subject subject = aSubject()
                .withSubjectConfirmation(subjectConfirmation)
                .withNameId(null)
                .build();

        subjectValidator.validate(subject, IN_RESPONSE_TO);
    }


}