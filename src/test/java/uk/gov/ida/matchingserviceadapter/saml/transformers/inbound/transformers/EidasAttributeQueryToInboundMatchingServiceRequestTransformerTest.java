package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.saml.security.AttributeQuerySignatureValidator;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.decorators.SamlAttributeQueryValidator;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

@RunWith(MockitoJUnitRunner.class)
public class EidasAttributeQueryToInboundMatchingServiceRequestTransformerTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    private SamlAttributeQueryValidator samlAttributeQueryValidator;

    @Mock
    private AttributeQuerySignatureValidator attributeQuerySignatureValidator;

    @Mock
    private AttributeQuery attributeQuery;

    @InjectMocks
    private EidasAttributeQueryToInboundMatchingServiceRequestTransformer transformer;

    @Test
    public void theAttributeQuerySignatureIsValidated() {
        exception.expect(RuntimeException.class);

        transformer.apply(attributeQuery);

        exception.expectMessage("TODO: Signature valid. Next stage of eIDAS MSA development");
        verify(samlAttributeQueryValidator).validate(attributeQuery);
        verify(attributeQuerySignatureValidator).validate(attributeQuery);
        verifyNoMoreInteractions(samlAttributeQueryValidator, attributeQuerySignatureValidator);
    }
}