package uk.gov.ida.matchingserviceadapter.validators;

import org.opensaml.saml.saml2.core.Audience;
import uk.gov.ida.validation.messages.MessageImpl;
import uk.gov.ida.validation.validators.CompositeValidator;
import uk.gov.ida.validation.validators.FixedErrorValidator;
import uk.gov.ida.validation.validators.RequiredValidator;

import java.util.function.Function;

import static uk.gov.ida.validation.messages.MessageImpl.fieldMessage;

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
