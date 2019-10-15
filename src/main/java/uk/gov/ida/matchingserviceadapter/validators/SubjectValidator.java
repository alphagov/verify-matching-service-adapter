package uk.gov.ida.matchingserviceadapter.validators;

import com.google.inject.Inject;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.NameIDType;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import uk.gov.ida.saml.core.validation.SamlResponseValidationException;

import java.util.stream.Stream;

public class SubjectValidator {
    AssertionTimeRestrictionValidator timeRestrictionValidator;

    @Inject
    public SubjectValidator(AssertionTimeRestrictionValidator timeRestrictionValidator) {
        this.timeRestrictionValidator = timeRestrictionValidator;
    }

    // FIXME: evaluate the expectedInResponseTo arg once Hub unsigned assertions PR supplies the correct value
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

        if (subject.getNameID().getFormat() == null || subject.getNameID().getFormat().length() == 0) {
            throw new SamlResponseValidationException("NameID format is missing or empty in the subject of the assertion.");
        }

        boolean correctNameIdType = Stream
                .of(NameIDType.PERSISTENT, NameIDType.TRANSIENT)
                .anyMatch(type -> type.equals(subject.getNameID().getFormat()));

        if (!correctNameIdType) {
            throw new SamlResponseValidationException("NameID format is invalid in the subject of the assertion.");
        }
    }
}

