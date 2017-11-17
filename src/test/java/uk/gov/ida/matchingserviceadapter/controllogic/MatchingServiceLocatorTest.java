package uk.gov.ida.matchingserviceadapter.controllogic;

import com.google.common.collect.ImmutableMap;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.w3c.dom.Document;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.services.MatchingService;

import java.util.function.Predicate;

import static org.assertj.core.api.Assertions.anyOf;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MatchingServiceLocatorTest {

    @InjectMocks
    private MatchingServiceLocator matchingServiceLocator;

    @Mock
    private MatchingService service1, service2;

    @Mock
    private Predicate<MatchingServiceRequestContext> predicate1, predicate2;

    @Mock
    private Document attributeQueryDocument;

    @Before
    public void setup() {
        matchingServiceLocator = new MatchingServiceLocator(
            ImmutableMap.of(
                predicate1, service1,
                predicate2, service2
            )
        );
    }

    @Test
    public void shouldDispatchToCorrectService() {
        when(predicate1.test(any(MatchingServiceRequestContext.class))).thenReturn(false);
        when(predicate2.test(any(MatchingServiceRequestContext.class))).thenReturn(true);
        assertThat(matchingServiceLocator.findServiceFor(new MatchingServiceRequestContext(attributeQueryDocument))).isEqualTo(service2);
    }

    @Test(expected = RuntimeException.class)
    public void shouldThrowIfNoServiceFound() {
        when(predicate1.test(any(MatchingServiceRequestContext.class))).thenReturn(false);
        when(predicate2.test(any(MatchingServiceRequestContext.class))).thenReturn(false);
        matchingServiceLocator.findServiceFor(new MatchingServiceRequestContext(attributeQueryDocument));
    }

}