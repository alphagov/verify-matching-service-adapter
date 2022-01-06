package uk.gov.ida.matchingserviceadapter.exceptions;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;

import javax.inject.Inject;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.text.MessageFormat;
import java.util.UUID;

public class ExceptionExceptionMapper implements ExceptionMapper<Exception> {

    private static final Logger LOG = LoggerFactory.getLogger(ExceptionExceptionMapper.class);
    private final MatchingServiceAdapterConfiguration configuration;

    @Inject
    public ExceptionExceptionMapper(MatchingServiceAdapterConfiguration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Response toResponse(Exception exception) {
        if (exception instanceof NotFoundException) {
            return Response.status(Response.Status.NOT_FOUND).build();
        }
        UUID eventId = UUID.randomUUID();
        LOG.error(MessageFormat.format("{0} - Exception while processing request.", eventId), exception);
        StringBuilder sb = new StringBuilder();
        sb.append(exception.getClass().getName());

        if (configuration.getReturnStackTraceInErrorResponse()) {
            sb.append(" : ");
            sb.append(exception.getMessage());
            sb.append("\n");
            for (StackTraceElement element : exception.getStackTrace()) {
                sb.append("! ");
                sb.append(element.toString());
                sb.append("\n");
            }
        }
        return Response.serverError().entity(sb.toString()).build();
    }
}
