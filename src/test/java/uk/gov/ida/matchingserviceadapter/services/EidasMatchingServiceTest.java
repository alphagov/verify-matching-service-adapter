package uk.gov.ida.matchingserviceadapter.services;

import com.google.common.collect.ImmutableList;
import org.beanplanet.messages.domain.Message;
import org.beanplanet.messages.domain.Messages;
import org.beanplanet.validation.Validator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.w3c.dom.Document;
import uk.gov.ida.matchingserviceadapter.domain.EidasLoa;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.VerifyMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.exceptions.AttributeQueryValidationException;
import uk.gov.ida.matchingserviceadapter.mappers.MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper;
import uk.gov.ida.matchingserviceadapter.proxies.MatchingServiceProxy;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.builders.AssertionBuilder;
import uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder;

import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.beanplanet.messages.domain.MessageImpl.globalMessage;
import static org.beanplanet.messages.domain.MessagesImpl.messages;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anEidasAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static uk.gov.ida.saml.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;

@RunWith(OpenSAMLMockitoRunner.class)
public class EidasMatchingServiceTest {

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Mock
    private Document document;
    @Mock
    private Validator<AttributeQuery> validator;
    @Mock
    private MatchingServiceRequestDto matchingServiceRequestDto;
    @Mock
    private MatchingServiceResponseDto matchingServiceResponseDto;
    @Mock
    OutboundResponseFromMatchingService outboundResponseFromMatchingService;
    @Mock
    private Function<MatchingServiceRequestContext, MatchingServiceRequestDto> transformer;
    @Mock
    private MatchingServiceProxy matchingServiceClient;
    @Mock
    private MatchingServiceResponseDtoToOutboundResponseFromMatchingServiceMapper responseMapper;

    private static final String PID = "pid";
    private static final AttributeQuery ATTRIBUTE_QUERY = AttributeQueryBuilder.anAttributeQuery().build();
    private static final Assertion ASSERTION = AssertionBuilder.anAssertion().addAttributeStatement(anEidasAttributeStatement().build())
        .addAuthnStatement(
            anAuthnStatement()
                .withAuthnContext(
                    anAuthnContext()
                        .withAuthnContextClassRef(
                            anAuthnContextClassRef()
                                .withAuthnContextClasRefValue(EidasLoa.HIGH.getValueUri())
                                .build()
                        )
                        .build()

                ).build()).buildUnencrypted();
    private EidasMatchingService service;
    private MatchingServiceRequestContext request;

    @Before
    public void setup() {
        service = new EidasMatchingService(validator, transformer, matchingServiceClient, responseMapper);
        request = new MatchingServiceRequestContext(document, ATTRIBUTE_QUERY, ImmutableList.of(ASSERTION));
    }

    @Test
    public void shouldReturnErrorsWhenValidationFails() {
        Message validationErrorMessage = globalMessage("theValidationErrorCode", "handle failed with validation messages");
        exception.expectMessage("Eidas Attribute Query was invalid");
        exception.expectMessage(validationErrorMessage.getParameterisedMessage());
        exception.expect(AttributeQueryValidationException.class);
        when(validator.validate(any(AttributeQuery.class), any(Messages.class))).thenReturn(messages().addError(validationErrorMessage));

        service.handle(request);
    }

    @Test
    public void shouldHandleValidAQR() {
        when(validator.validate(eq(ATTRIBUTE_QUERY), any(Messages.class))).thenReturn(messages());
        when(transformer.apply(request)).thenReturn(matchingServiceRequestDto);
        when(matchingServiceClient.makeMatchingServiceRequest(matchingServiceRequestDto)).thenReturn(matchingServiceResponseDto);
        when(matchingServiceRequestDto.getHashedPid()).thenReturn(PID);
        when(responseMapper.map(matchingServiceResponseDto, PID, request.getAttributeQuery().getID(), request.getAttributeQuery().getSubject().getNameID().getNameQualifier(),
            AuthnContext.LEVEL_3, request.getAttributeQuery().getSubject().getNameID().getSPNameQualifier()))
            .thenReturn(outboundResponseFromMatchingService);

        VerifyMatchingServiceResponse response = (VerifyMatchingServiceResponse) service.handle(request);

        verify(validator).validate(eq(ATTRIBUTE_QUERY), any(Messages.class));
        verify(transformer).apply(request);
        verify(matchingServiceClient).makeMatchingServiceRequest(matchingServiceRequestDto);
        verify(responseMapper).map(matchingServiceResponseDto, PID, request.getAttributeQuery().getID(), request.getAttributeQuery().getSubject().getNameID().getNameQualifier(),
            AuthnContext.LEVEL_3, request.getAttributeQuery().getSubject().getNameID().getSPNameQualifier());
        assertThat(response.getOutboundResponseFromMatchingService()).isEqualTo(outboundResponseFromMatchingService);
        verifyNoMoreInteractions(validator, transformer, matchingServiceClient);
    }

}
