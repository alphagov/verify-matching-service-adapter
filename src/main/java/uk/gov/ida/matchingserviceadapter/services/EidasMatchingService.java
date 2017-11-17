package uk.gov.ida.matchingserviceadapter.services;


import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.Validator;

import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class EidasMatchingService implements MatchingService {
    public static final String TODO_MESSAGE = "TODO: Eidas Attribute Query and its identity assertion signatures are valid." +
        " Next stage of eIDAS MSA development is to validate the AQR";

    private Validator<AttributeQuery> validator;

    public EidasMatchingService(Validator<AttributeQuery> validator) {
        this.validator = validator;
    }

    public Validator<AttributeQuery> getValidator() {
        return validator;
    }

    @Override
    public MatchingServiceResponse handle(MatchingServiceRequestContext request) {
        Messages validationMessages = validator.validate(request.getAttributeQuery(), messages());
        if (validationMessages.hasErrors()) {
            throw new RuntimeException("Eidas Attribute Query was invalid: " + validationMessages);
        }

        // TODO - EID-202 handle attributes and fill out the nulls below
        throw new RuntimeException(TODO_MESSAGE);
    }
}
