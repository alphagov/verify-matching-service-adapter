package uk.gov.ida.matchingserviceadapter.validators;

import com.google.common.collect.ImmutableSet;
import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import uk.gov.ida.matchingserviceadapter.validators.exceptions.SamlResponseValidationException;

import java.util.Set;
import java.util.stream.Collectors;

import static org.opensaml.saml.saml2.core.SubjectConfirmation.METHOD_BEARER;

public class SubjectValidator {
    private final TimeRestrictionValidator timeRestrictionValidator;
    public static final Set<String> VALID_IDENTIFIERS = ImmutableSet.of(NameID.PERSISTENT);

    public SubjectValidator(TimeRestrictionValidator timeRestrictionValidator) {
        this.timeRestrictionValidator = timeRestrictionValidator;
    }

    public void validate(Subject subject, String expectedInResponseTo) {
        if (subject == null) {
            throw new SamlResponseValidationException("Subject is missing from the assertion.");
        }

        if (subject.getSubjectConfirmations().size() != 1) {
            throw new SamlResponseValidationException("Exactly one subject confirmation is expected.");
        }

        SubjectConfirmation subjectConfirmation = subject.getSubjectConfirmations().get(0);
        if (!METHOD_BEARER.equals(subjectConfirmation.getMethod())) {
            throw new SamlResponseValidationException("Subject confirmation method must be 'bearer'.");
        }

        SubjectConfirmationData subjectConfirmationData = subjectConfirmation.getSubjectConfirmationData();
        if (subjectConfirmationData == null) {
            throw new SamlResponseValidationException("Subject confirmation data is missing from the assertion.");
        }

        timeRestrictionValidator.validateNotBefore(subjectConfirmationData.getNotBefore());

        DateTime notOnOrAfter = subjectConfirmationData.getNotOnOrAfter();
        if (notOnOrAfter == null) {
            throw new SamlResponseValidationException("Subject confirmation data must contain 'NotOnOrAfter'.");
        }

        timeRestrictionValidator.validateNotOnOrAfter(notOnOrAfter);

        String actualInResponseTo = subjectConfirmationData.getInResponseTo();
        if (actualInResponseTo == null) {
            throw new SamlResponseValidationException("Subject confirmation data must contain 'InResponseTo'.");
        }

        if (!expectedInResponseTo.equals(actualInResponseTo)) {
            throw new SamlResponseValidationException(String.format("'InResponseTo' must match requestId. Expected %s but was %s", expectedInResponseTo, actualInResponseTo));
        }

        if (subject.getNameID() == null) {
            throw new SamlResponseValidationException("NameID is missing from the subject of the assertion.");
        }

        if (!VALID_IDENTIFIERS.contains(subject.getNameID().getFormat())) {
            throw new SamlResponseValidationException(String.format("NameID [%s] is not in the correct format. It needs to be %s", subject.getNameID().getFormat(), VALID_IDENTIFIERS.stream().collect(Collectors.joining(","))));
        }

        if (null == subjectConfirmationData.getRecipient()) {
            throw new SamlResponseValidationException("Subject confirmation data must contain 'Recipient'.");
        }
    }
}
