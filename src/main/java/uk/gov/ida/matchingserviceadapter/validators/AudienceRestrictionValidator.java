package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.saml.saml2.core.AudienceRestriction;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.FixedErrorValidator;
import uk.gov.ida.validation.validators.RequiredValidator;

import java.util.function.Function;

import static uk.gov.ida.validation.messages.MessageImpl.fieldMessage;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;

public class AudienceRestrictionValidator<T> extends CompositeValidator<T> {
    public static final MessageImpl DEFAULT_REQUIRED_MESSAGE = fieldMessage("audienceRestriction", "audienceRestriction.empty", "The audience restriction was not provided.");
    public static final MessageImpl DEFAULT_AUDIENCES_MUST_CONTAIN_ONE_AUDIENCE_MESSAGE = globalMessage("audienceRestriction.audiences.audience", "There must be 1 audience.");

    public AudienceRestrictionValidator(final Function<T, AudienceRestriction> valueProvider, final String audienceUri) {
        super(
            true,
            valueProvider,
            new RequiredValidator<>(DEFAULT_REQUIRED_MESSAGE),
            new FixedErrorValidator<>(audienceRestriction -> audienceRestriction.getAudiences().size() != 1, DEFAULT_AUDIENCES_MUST_CONTAIN_ONE_AUDIENCE_MESSAGE),
            new AudienceValidator<>(audienceRestriction -> audienceRestriction.getAudiences().get(0), audienceUri)
        );
    }
}
