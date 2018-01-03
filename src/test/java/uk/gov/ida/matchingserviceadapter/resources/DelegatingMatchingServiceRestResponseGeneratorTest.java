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

public class DelegatingMatchingServiceRestResponseGeneratorTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldSaveDelegatesList() {
        ImmutableMap<Class<?>, MatchingServiceRestResponseGenerator> delegates = ImmutableMap.of();
        DelegatingMatchingServiceRestResponseGenerator renderer = new DelegatingMatchingServiceRestResponseGenerator(delegates);

        assertThat(renderer.getDelegates(), sameInstance(delegates));
    }

    @Test
    public void renderErrorsWhenNoDelegateIsFound() {
        exception.expectMessage("No delegate found for matching service response");

        ImmutableMap<Class<?>, MatchingServiceRestResponseGenerator> delegates = ImmutableMap.of(DelegatingMatchingServiceRestResponseGeneratorTest.class, mock(MatchingServiceRestResponseGenerator.class));
        DelegatingMatchingServiceRestResponseGenerator renderer = new DelegatingMatchingServiceRestResponseGenerator(delegates);

        renderer.generateResponse(mock(MatchingServiceResponse.class));
    }

    @Test
    public void renderDelegatesToConfiguredResponseRenderer() {
        MatchingServiceRestResponseGenerator notSelectedRenderer = mock(MatchingServiceRestResponseGenerator.class);
        MatchingServiceRestResponseGenerator selectedRenderer = mock(MatchingServiceRestResponseGenerator.class);
        ImmutableMap<Class<?>, MatchingServiceRestResponseGenerator> delegates = ImmutableMap.of(
            MatchingServiceResponse.class, notSelectedRenderer,
            VerifyMatchingServiceResponse.class, selectedRenderer);
        VerifyMatchingServiceResponse response = new VerifyMatchingServiceResponse(mock(OutboundResponseFromMatchingService.class));
        DelegatingMatchingServiceRestResponseGenerator renderer = new DelegatingMatchingServiceRestResponseGenerator(delegates);

        renderer.generateResponse(response);

        verify(selectedRenderer).generateResponse(response);
        verifyNoMoreInteractions(notSelectedRenderer, selectedRenderer);
    }
}