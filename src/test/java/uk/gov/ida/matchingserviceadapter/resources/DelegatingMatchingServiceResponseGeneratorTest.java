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

public class DelegatingMatchingServiceResponseGeneratorTest {
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Test
    public void shouldSaveDelegatesList() {
        ImmutableMap<Class<?>, MatchingServiceResponseGenerator> delegates = ImmutableMap.of();
        DelegatingMatchingServiceResponseGenerator generator = new DelegatingMatchingServiceResponseGenerator(delegates);

        assertThat(generator.getDelegates(), sameInstance(delegates));
    }

    @Test
    public void shouldThrowExceptionWhenNoDelegateIsFound() {
        exception.expectMessage("No delegate found for matching service response");

        ImmutableMap<Class<?>, MatchingServiceResponseGenerator> delegates = ImmutableMap.of(DelegatingMatchingServiceResponseGeneratorTest.class, mock(MatchingServiceResponseGenerator.class));
        DelegatingMatchingServiceResponseGenerator generator = new DelegatingMatchingServiceResponseGenerator(delegates);

        generator.generateResponse(mock(MatchingServiceResponse.class));
    }

    @Test
    public void shouldCallCorrectResponseGeneratorByResponseType() {
        MatchingServiceResponseGenerator notSelectedGenerator = mock(MatchingServiceResponseGenerator.class);
        MatchingServiceResponseGenerator selectedGenerator = mock(MatchingServiceResponseGenerator.class);
        ImmutableMap<Class<?>, MatchingServiceResponseGenerator> delegates = ImmutableMap.of(
            MatchingServiceResponse.class, notSelectedGenerator,
            VerifyMatchingServiceResponse.class, selectedGenerator);
        VerifyMatchingServiceResponse response = new VerifyMatchingServiceResponse(mock(OutboundResponseFromMatchingService.class));
        DelegatingMatchingServiceResponseGenerator generator = new DelegatingMatchingServiceResponseGenerator(delegates);

        generator.generateResponse(response);

        verify(selectedGenerator).generateResponse(response);
        verifyNoMoreInteractions(notSelectedGenerator, selectedGenerator);
    }
}