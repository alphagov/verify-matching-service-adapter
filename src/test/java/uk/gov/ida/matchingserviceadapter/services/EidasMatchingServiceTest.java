package uk.gov.ida.matchingserviceadapter.services;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.w3c.dom.Document;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.validation.messages.Message;
import uk.gov.ida.validation.messages.Messages;
import uk.gov.ida.validation.validators.Validator;

import java.util.Collections;
import java.util.function.Function;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.ida.validation.messages.MessageImpl.globalMessage;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;


@RunWith(MockitoJUnitRunner.class)
public class EidasMatchingServiceTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private Validator<AttributeQuery> validator;

    @Mock
    private Function<MatchingServiceRequestContext, MatchingServiceRequestDto> transformer;

    @SuppressWarnings("unchecked")
    @Test
    public void serviceIsCreatedSucessfully() {
        EidasMatchingService service = new EidasMatchingService(validator, transformer);

        assertThat(service.getValidator(), sameInstance(validator));
        assertThat(service.getTransformer(), sameInstance(transformer));
    }

    @Test
    public void handleFailsAndReturnsValidationErrors() {
        Message validationErrorMessage = globalMessage("theValidationErrorCode", "handle failed with validation messages");
        exception.expectMessage("Eidas Attribute Query was invalid");
        exception.expectMessage(validationErrorMessage.getParameterisedMessage());
        exception.expect(RuntimeException.class);

        when(validator.validate(any(AttributeQuery.class), any(Messages.class))).thenReturn(messages().addError(validationErrorMessage));
        EidasMatchingService service = new EidasMatchingService(validator, transformer);
        Document attributeQueryDocument = mock(Document.class);
        AttributeQuery attributeQuery = mock(AttributeQuery.class);
        MatchingServiceRequestContext request = new MatchingServiceRequestContext(attributeQueryDocument, attributeQuery, Collections.emptyList());

        service.handle(request);
    }

    @Test
    public void handleSuccessful() {
        exception.expectMessage(EidasMatchingService.TODO_MESSAGE);

        when(validator.validate(any(AttributeQuery.class), any(Messages.class))).thenReturn(messages());
        EidasMatchingService service = new EidasMatchingService(validator, transformer);
        Document attributeQueryDocument = mock(Document.class);
        AttributeQuery attributeQuery = mock(AttributeQuery.class);
        MatchingServiceRequestContext request = new MatchingServiceRequestContext(attributeQueryDocument, attributeQuery, Collections.emptyList());

        service.handle(request);

        verify(validator).validate(attributeQuery, any(Messages.class));
        verify(transformer).apply(request);
        verifyNoMoreInteractions(validator, transformer);
    }
}
