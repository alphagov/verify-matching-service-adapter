package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.MessageImpl;
import org.beanplanet.validation.CompositeValidator;
import org.beanplanet.validation.FixedErrorValidator;
import org.beanplanet.validation.RequiredValidator;
import org.opensaml.saml.saml2.core.AudienceRestriction;

import java.util.function.Function;

import static org.beanplanet.messages.domain.MessageImpl.fieldMessage;
import static org.beanplanet.messages.domain.MessageImpl.globalMessage;

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
