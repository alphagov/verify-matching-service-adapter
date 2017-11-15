package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers;

import com.google.inject.Inject;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.Validator;

import java.util.function.Function;

import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class EidasAttributeQueryToInboundMatchingServiceRequestTransformer implements Function<AttributeQuery, InboundMatchingServiceRequest> {
    public static final String TODO_MESSAGE = "TODO: Eidas Attribute Query and its identity assertion signatures are valid." +
        " Next stage of eIDAS MSA development is to validate the AQR";
    private final Validator<AttributeQuery> attributeQueryValidator;

    @Inject
    public EidasAttributeQueryToInboundMatchingServiceRequestTransformer(final Validator<AttributeQuery> attributeQueryValidator) {
        this.attributeQueryValidator = attributeQueryValidator;
    }

    @Override
    public InboundMatchingServiceRequest apply(AttributeQuery attributeQuery) {
        Messages validationMessages = attributeQueryValidator.validate(attributeQuery, messages());
        if (validationMessages.hasErrors()) {
            throw new RuntimeException("Eidas Attribute Query was invalid: " + validationMessages);
        }

        // TODO - EID-202 handle attributes and fill out the nulls below
        throw new RuntimeException(TODO_MESSAGE);
    }
}
