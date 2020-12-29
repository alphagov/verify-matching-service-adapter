package uk.gov.ida.matchingserviceadapter.domain;

import org.joda.time.DateTime;
import uk.gov.ida.saml.core.domain.IdaMatchingServiceResponse;

import java.util.UUID;

import static java.text.MessageFormat.format;

public class HealthCheckResponseFromMatchingService extends IdaMatchingServiceResponse {
    
    public HealthCheckResponseFromMatchingService(String entityId, String healthCheckRequestId, String msaVersion, boolean eidasEnabled, boolean shouldSignWithSHA1, boolean universalMatchingDataset) {
        super(format("healthcheck-response-id-{0}-version-{1}-eidasenabled-{2}-shouldsignwithsha1-{3}-universalMatchingDataset-{4}", UUID.randomUUID(), msaVersion, eidasEnabled, shouldSignWithSHA1, universalMatchingDataset), healthCheckRequestId, entityId, DateTime.now());
    }
}

