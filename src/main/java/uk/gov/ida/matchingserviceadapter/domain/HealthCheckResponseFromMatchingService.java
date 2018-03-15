package uk.gov.ida.matchingserviceadapter.domain;

import org.joda.time.DateTime;
import uk.gov.ida.saml.core.domain.IdaMatchingServiceResponse;

import java.util.UUID;

import static java.text.MessageFormat.format;

public class HealthCheckResponseFromMatchingService extends IdaMatchingServiceResponse {
    public HealthCheckResponseFromMatchingService(String entityId, String healthCheckRequestId, String msaVersion) {
        super(format("healthcheck-response-id-{0}-version-{1}", UUID.randomUUID(), msaVersion), healthCheckRequestId, entityId, DateTime.now());
    }
}

