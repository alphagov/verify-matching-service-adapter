package uk.gov.ida.matchingserviceadapter.exceptions;


import com.squarespace.jersey2.guice.JerseyGuiceUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import uk.gov.ida.common.ExceptionType;
import uk.gov.ida.exceptions.ApplicationException;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;

import javax.ws.rs.core.Response;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ExceptionExceptionMapperTest {

    static {
        JerseyGuiceUtils.reset();
    }

    @Mock
    private MatchingServiceAdapterConfiguration configuration;

    @Test
    public void shouldNotRespondWithTheBodyOfTheUpstreamError() {
        ExceptionExceptionMapper exceptionExceptionMapper = new ExceptionExceptionMapper(configuration);

        String causeMessage = "my message";
        ApplicationException unauditedException = ApplicationException.createUnauditedException(ExceptionType.CLIENT_ERROR, causeMessage, new Exception());
        Response response = exceptionExceptionMapper.toResponse(unauditedException);

        String responseBody = (String) response.getEntity();
        assertThat(responseBody).contains("uk.gov.ida.exceptions.ApplicationException - Cause: java.lang.Exception");
        assertThat(responseBody).doesNotContain(causeMessage);
    }

    @Test
    public void shouldRespondWithTheBodyOfTheUpstreamErrorWhenReturningCauseStackTrace() {
        when(configuration.getReturnStackTraceInResponse()).thenReturn(true);
        ExceptionExceptionMapper exceptionExceptionMapper = new ExceptionExceptionMapper(configuration);

        String causeMessage = "my message";
        ApplicationException unauditedException = ApplicationException.createUnauditedException(ExceptionType.CLIENT_ERROR, causeMessage, new Exception());
        Response response = exceptionExceptionMapper.toResponse(unauditedException);

        String responseBody = (String) response.getEntity();
        assertThat(responseBody).contains("uk.gov.ida.exceptions.ApplicationException - Cause: java.lang.Exception");
        assertThat(responseBody).contains(causeMessage);
    }
}
