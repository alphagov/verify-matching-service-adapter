package uk.gov.ida.matchingserviceadapter.services;


import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.exceptions.AttributeQueryValidationException;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.Validator;

import java.util.function.Function;

import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class EidasMatchingService implements MatchingService {
    public static final String TODO_MESSAGE = "TODO: Eidas Attribute Query and its identity assertion signatures are valid." +
        " Next stage of eIDAS MSA development is to validate the AQR";

    private Validator<AttributeQuery> validator;
    private Function<MatchingServiceRequestContext, MatchingServiceRequestDto> transformer;

    public EidasMatchingService(Validator<AttributeQuery> validator,
                                Function<MatchingServiceRequestContext, MatchingServiceRequestDto> transformer) {
        this.validator = validator;
        this.transformer = transformer;
    }

    public Validator<AttributeQuery> getValidator() {
        return validator;
    }

    public Function<MatchingServiceRequestContext, MatchingServiceRequestDto> getTransformer() {
        return transformer;
    }

    @Override
    public MatchingServiceResponse handle(MatchingServiceRequestContext request) {
        Messages validationMessages = validator.validate(request.getAttributeQuery(), messages());
        if (validationMessages.hasErrors()) {
            throw new AttributeQueryValidationException("Eidas Attribute Query was invalid: " + validationMessages);
        }

        MatchingServiceRequestDto matchingServiceRequestDto = transformer.apply(request);

        // EID-297: Use the MatchingServiceRequestDto created above tp return a MatchingServiceResponse

        // TODO - EID-202 handle attributes and fill out the nulls below
        throw new RuntimeException(TODO_MESSAGE);
    }
}
