package uk.gov.ida.matchingserviceadapter.validators;

import org.beanplanet.messages.domain.MessageImpl;
import org.beanplanet.validation.CompositeValidator;
import org.beanplanet.validation.FixedErrorValidator;
import org.beanplanet.validation.RequiredValidator;
import org.opensaml.saml.saml2.core.Audience;

import java.util.function.Function;

import static org.beanplanet.messages.domain.MessageImpl.fieldMessage;

public class AudienceValidator<T> extends CompositeValidator<T> {
    public static final MessageImpl DEFAULT_REQUIRED_MESSAGE = fieldMessage("audience", "audience.empty", "The audience was not provided.");
    public static final MessageImpl DEFAULT_REQUIRED_URI_MESSAGE = fieldMessage("audience.audienceUri", "audience.audienceUri.empty", "The audience uri was not provided.");

    public AudienceValidator(final Function<T, Audience> valueProvider, final String audienceUri) {
        super(
            true,
            valueProvider,
            new RequiredValidator<>(DEFAULT_REQUIRED_MESSAGE),
            new RequiredValidator<>(DEFAULT_REQUIRED_URI_MESSAGE, Audience::getAudienceURI),
            new FixedErrorValidator<>(a -> !a.getAudienceURI().equals(audienceUri), generateMismatchedAudienceUriMessage(audienceUri))
        );
    }

    public static MessageImpl generateMismatchedAudienceUriMessage(final String entityId) {
        return fieldMessage("audience.audienceUri", "audience.audienceUri.notMatched", "The audience uri value was not same as the entity id [" + entityId + "].");
    }
}
