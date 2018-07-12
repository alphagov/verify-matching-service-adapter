package uk.gov.ida.matchingserviceadapter.validators;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;

import static org.opensaml.saml.saml2.core.SubjectConfirmation.METHOD_BEARER;

public class SubjectValidator {
    TimeRestrictionValidator timeRestrictionValidator;

    @Inject
    public SubjectValidator(TimeRestrictionValidator timeRestrictionValidator) {
        this.timeRestrictionValidator = timeRestrictionValidator;
    }

    public void validate(Subject subject, String expectedInResponseTo) {
        if (subject == null) {
            throw new SamlResponseValidationException("Subject is missing from the assertion.");
        }

        if (subject.getSubjectConfirmations().size() == 0) {
            throw new SamlResponseValidationException("A subject confirmation is expected.");
        }

        SubjectConfirmation subjectConfirmation = subject.getSubjectConfirmations().get(0);


        SubjectConfirmationData subjectConfirmationData = subjectConfirmation.getSubjectConfirmationData();
        if (subjectConfirmationData == null) {
            throw new SamlResponseValidationException("Subject confirmation data is missing from the assertion.");
        }

        DateTime notOnOrAfter = subjectConfirmationData.getNotOnOrAfter();
        if (notOnOrAfter == null) {
            throw new SamlResponseValidationException("Subject confirmation data must contain 'NotOnOrAfter'.");
        }

        timeRestrictionValidator.validateNotOnOrAfter(notOnOrAfter);

        String actualInResponseTo = subjectConfirmationData.getInResponseTo();
        if (actualInResponseTo == null) {
            throw new SamlResponseValidationException("Subject confirmation data must contain 'InResponseTo'.");
        }

        if (subject.getNameID() == null) {
            throw new SamlResponseValidationException("NameID is missing from the subject of the assertion.");
        }
    }
}

