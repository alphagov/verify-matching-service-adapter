package uk.gov.ida.matchingserviceadapter.services;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.w3c.dom.Document;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.validation.messages.Message;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.Validator;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

public class EidasMatchingServiceTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @SuppressWarnings("unchecked")
    @Test
    public void constructor() {
        Validator<AttributeQuery> attributeQueryValidator = mock(Validator.class);
        EidasMatchingService service = new EidasMatchingService(attributeQueryValidator);

        assertThat(service.getValidator(), sameInstance(attributeQueryValidator));
    }

    @Test
    public void handleFailsAndReturnsValidationErrors() {
        Message validationErrorMessage = globalMessage("theValidationErrorCode", "handle failed with validation messages");
        exception.expectMessage("Eidas Attribute Query was invalid");
        exception.expectMessage(validationErrorMessage.getParameterisedMessage());

        Validator<AttributeQuery> attributeQueryValidator = mock(Validator.class);
        when(attributeQueryValidator.validate(any(AttributeQuery.class), any(Messages.class))).thenReturn(messages().addError(validationErrorMessage));
        EidasMatchingService service = new EidasMatchingService(attributeQueryValidator);
        Document attributeQueryDocument = mock(Document.class);
        AttributeQuery attributeQuery = mock(AttributeQuery.class);

        service.handle(new MatchingServiceRequestContext(attributeQueryDocument, attributeQuery, Collections.emptyList()));

        assertThat(service.getValidator(), sameInstance(attributeQueryValidator));
    }

    @Test
    public void handleSuccessful() {
        exception.expectMessage(EidasMatchingService.TODO_MESSAGE);

        Validator<AttributeQuery> attributeQueryValidator = mock(Validator.class);
        when(attributeQueryValidator.validate(any(AttributeQuery.class), any(Messages.class))).thenReturn(messages());
        EidasMatchingService service = new EidasMatchingService(attributeQueryValidator);
        Document attributeQueryDocument = mock(Document.class);
        AttributeQuery attributeQuery = mock(AttributeQuery.class);

        service.handle(new MatchingServiceRequestContext(attributeQueryDocument, attributeQuery, Collections.emptyList()));

        assertThat(service.getValidator(), sameInstance(attributeQueryValidator));
    }
}