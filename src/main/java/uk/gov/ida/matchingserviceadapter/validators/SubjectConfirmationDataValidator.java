package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.FixedErrorValidator;
import uk.gov.ida.validation.validators.RequiredValidator;

import java.util.function.Function;

public class SubjectConfirmationDataValidator<T> extends CompositeValidator<T> {

    public static final MessageImpl CONFIRMATION_DATA_NOT_PRESENT = MessageImpl.globalMessage("confirmationData.absent", "confirmation data must not be null");
    public static final MessageImpl NOT_ON_OR_AFTER_NOT_PRESENT = MessageImpl.fieldMessage("confirmationData.onOrAfter", "confirmationData.onOrAfter.absent", "Not on or after must not be null");
    public static final MessageImpl NOT_ON_OR_AFTER_INVALID = MessageImpl.fieldMessage("confirmationData.notOnOrAfter", "confirmationData.notOnOrAfter.invalid", "Not on or after must not be in the past");
    public static final MessageImpl NOT_BEFORE_INVALID = MessageImpl.fieldMessage("confirmationData.notBefore", "confirmationData.notBefore.invalid", "Not before must not be in the future");
    public static final MessageImpl IN_RESPONSE_TO_NOT_PRESENT = MessageImpl.fieldMessage("confirmationData.inResponseTo", "confirmationData.inResponseTo.absent", "In response to must not be null");
    public static final MessageImpl IN_RESPONSE_TO_NOT_WHAT_WAS_EXPECTED = MessageImpl.fieldMessage("confirmationData.inResponseTo", "confirmationData.inResponseTo.incorrect", "In response to was not what was expected");
    public static final MessageImpl RECIPIENT_NOT_PRESENT = MessageImpl.fieldMessage("confirmationData.recipient", "confirmationData.recipient.absent", "Recipient must not be null");

    public SubjectConfirmationDataValidator(Function<T, SubjectConfirmationData> valueProvider, TimeRestrictionValidator timeRestrictionValidator, String requestId) {
        super(
            true,
            valueProvider,
            new RequiredValidator<>(CONFIRMATION_DATA_NOT_PRESENT),
            new RequiredValidator<>(NOT_ON_OR_AFTER_NOT_PRESENT, SubjectConfirmationData::getNotOnOrAfter),
            new FixedErrorValidator<>(scd -> {
                try {
                    timeRestrictionValidator.validateNotOnOrAfter(scd.getNotOnOrAfter());
                    return false;
                }
                catch(Exception e) {
                    return true;
                }
            }, NOT_ON_OR_AFTER_INVALID),
            new FixedErrorValidator<>(scd -> {
                try {
                    timeRestrictionValidator.validateNotBefore(scd.getNotBefore());
                    return false;
                }
                catch(Exception e) {
                    return true;
                }
            }, NOT_BEFORE_INVALID),
            new RequiredValidator<>(IN_RESPONSE_TO_NOT_PRESENT, SubjectConfirmationData::getInResponseTo),
            new FixedErrorValidator<>(scd -> !scd.getInResponseTo().equals(requestId), IN_RESPONSE_TO_NOT_WHAT_WAS_EXPECTED),
            new RequiredValidator<>(RECIPIENT_NOT_PRESENT, SubjectConfirmationData::getRecipient)
        );
    }

}
