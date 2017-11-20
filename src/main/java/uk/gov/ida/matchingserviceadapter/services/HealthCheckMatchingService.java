package uk.gov.ida.matchingserviceadapter.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.utils.manifest.ManifestReader;

public class HealthCheckMatchingService implements MatchingService {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckMatchingService.class);

    private final ManifestReader manifestReader;
    private final MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration;


    public HealthCheckMatchingService(ManifestReader manifestReader, MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration) {
        this.manifestReader = manifestReader;
        this.matchingServiceAdapterConfiguration = matchingServiceAdapterConfiguration;
        System.out.println("#######################Version: " + manifestReader.getValue("Version-Number"));
    }

    @Override
    public MatchingServiceResponse handle(MatchingServiceRequestContext request) {
        String requestId = request.getAttributeQuery().getID();
        LOG.info("Responding to health check with id '{}'.", requestId);
        return new HealthCheckMatchingServiceResponse(new HealthCheckResponseFromMatchingService(matchingServiceAdapterConfiguration.getEntityId(), requestId, manifestReader.getValue("Version-Number")));
    }
}
