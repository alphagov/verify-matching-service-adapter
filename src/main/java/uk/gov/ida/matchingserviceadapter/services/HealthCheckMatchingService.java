package uk.gov.ida.matchingserviceadapter.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterApplication;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.HealthCheckResponseFromMatchingService;
import uk.gov.ida.shared.utils.manifest.ManifestReader;

import java.io.IOException;

public class HealthCheckMatchingService implements MatchingService {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckMatchingService.class);

    private final ManifestReader manifestReader;
    private final MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration;


    public HealthCheckMatchingService(ManifestReader manifestReader, MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration) {
        this.manifestReader = manifestReader;
        this.matchingServiceAdapterConfiguration = matchingServiceAdapterConfiguration;
    }

    @Override
    public MatchingServiceResponse handle(MatchingServiceRequestContext request) {
        String requestId = request.getAttributeQuery().getID();
        LOG.info("Responding to health check with id '{}'.", requestId);

        String manifestVersionNumber = "UNKNOWN_VERSION_NUMBER";
        try {
            manifestVersionNumber = manifestReader.getAttributeValueFor(MatchingServiceAdapterApplication.class, "Version-Number");
        } catch (IOException e) {
            LOG.error("Failed to read version number from manifest", e);
        }

        return new HealthCheckMatchingServiceResponse(new HealthCheckResponseFromMatchingService(
            matchingServiceAdapterConfiguration.getEntityId(),
            requestId,
            manifestVersionNumber
        ));
    }
}
