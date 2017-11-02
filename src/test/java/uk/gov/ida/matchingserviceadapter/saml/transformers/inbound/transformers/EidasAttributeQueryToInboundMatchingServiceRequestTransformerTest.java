package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.validators.EidasAttributeQueryValidator;
import uk.gov.ida.validation.messages.MessagesImpl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers.EidasAttributeQueryToInboundMatchingServiceRequestTransformer.TODO_MESSAGE;
import static uk.gov.ida.saml.core.test.builders.AttributeQueryBuilder.anAttributeQuery;
import static uk.gov.ida.validation.messages.MessagesImpl.messages;

@RunWith(MockitoJUnitRunner.class)
public class EidasAttributeQueryToInboundMatchingServiceRequestTransformerTest {
    @Mock
    private EidasAttributeQueryValidator eidasAttributeQueryValidator;

    @InjectMocks
    private EidasAttributeQueryToInboundMatchingServiceRequestTransformer transformer;

    @Test
    public void theAttributeQuerySignatureIsValidated() {
        AttributeQuery attributeQuery = anAttributeQuery().build();
        MessagesImpl messages = messages();
        when(eidasAttributeQueryValidator.validate(attributeQuery, messages)).thenReturn(messages);

        try {
            transformer.apply(attributeQuery);
        } catch (RuntimeException e) {
            assertThat(e.getMessage()).isEqualTo(TODO_MESSAGE);
        }
        verify(eidasAttributeQueryValidator).validate(attributeQuery, messages);
    }
}
