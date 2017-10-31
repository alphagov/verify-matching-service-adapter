package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;

import java.util.function.Function;
import java.util.function.Predicate;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class DiscriminatingAttributeQueryToInboundMSRequestTransformerTest {
    @Mock
    private Predicate<AttributeQuery> eidasAttributeQuery;

    @Mock
    private Function<AttributeQuery, InboundMatchingServiceRequest> verifyTransformer;

    @Mock
    private Function<AttributeQuery, InboundMatchingServiceRequest> eidasTransformer;

    @Mock
    private AttributeQuery attributeQuery;

    private DiscriminatingAttributeQueryToInboundMSRequestTransformer transformer;

    @Before
    public void setup() {
        transformer = new DiscriminatingAttributeQueryToInboundMSRequestTransformer(eidasAttributeQuery, verifyTransformer, eidasTransformer);
    }


    @Test
    public void shouldCallVerifyTransformerForVerifyAttributeQuery() {
        when(eidasAttributeQuery.test(any(AttributeQuery.class))).thenReturn(false);

        transformer.apply(attributeQuery);

        verify(verifyTransformer).apply(attributeQuery);
        verifyNoMoreInteractions(verifyTransformer, eidasTransformer);
    }

    @Test
    public void shouldCallEidasTransformerForEidasAttributeQuery() {
        when(eidasAttributeQuery.test(any(AttributeQuery.class))).thenReturn(true);

        transformer.apply(attributeQuery);

        verify(eidasTransformer).apply(attributeQuery);
        verifyNoMoreInteractions(verifyTransformer, eidasTransformer);
    }
}