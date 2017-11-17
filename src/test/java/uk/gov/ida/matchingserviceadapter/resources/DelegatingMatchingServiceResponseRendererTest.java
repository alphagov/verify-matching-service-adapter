package uk.gov.ida.matchingserviceadapter.resources;

import com.google.common.collect.ImmutableMap;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.VerifyMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromMatchingService;

import static org.hamcrest.CoreMatchers.sameInstance;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

public class DelegatingMatchingServiceResponseRendererTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void constructor() {
        ImmutableMap<Class<?>, MatchingServiceResponseRenderer> delegates = ImmutableMap.of();
        DelegatingMatchingServiceResponseRenderer renderer = new DelegatingMatchingServiceResponseRenderer(delegates);

        assertThat(renderer.getDelegates(), sameInstance(delegates));
    }

    @Test
    public void renderErrorsWhenNoDelegateIsFound() {
        exception.expectMessage("No delegate found for matching service response");

        ImmutableMap<Class<?>, MatchingServiceResponseRenderer> delegates = ImmutableMap.of(DelegatingMatchingServiceResponseRendererTest.class, mock(MatchingServiceResponseRenderer.class));
        DelegatingMatchingServiceResponseRenderer renderer = new DelegatingMatchingServiceResponseRenderer(delegates);

        renderer.render(mock(MatchingServiceResponse.class));
    }

    @Test
    public void renderDelegatesToConfiguredResponseRenderer() {
        MatchingServiceResponseRenderer notSelectedRenderer = mock(MatchingServiceResponseRenderer.class);
        MatchingServiceResponseRenderer selectedRenderer = mock(MatchingServiceResponseRenderer.class);
        ImmutableMap<Class<?>, MatchingServiceResponseRenderer> delegates = ImmutableMap.of(
            MatchingServiceResponse.class, notSelectedRenderer,
            VerifyMatchingServiceResponse.class, selectedRenderer);
        VerifyMatchingServiceResponse response = new VerifyMatchingServiceResponse(mock(OutboundResponseFromMatchingService.class));
        DelegatingMatchingServiceResponseRenderer renderer = new DelegatingMatchingServiceResponseRenderer(delegates);

        renderer.render(response);

        verify(selectedRenderer).render(response);
        verifyNoMoreInteractions(notSelectedRenderer, selectedRenderer);
    }
}