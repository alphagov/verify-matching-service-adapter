package uk.gov.ida.matchingserviceadapter.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterApplication;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.shared.utils.manifest.ManifestReader;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.function.Function;

import static javax.ws.rs.core.Response.ok;

public class MatchingResponseGenerator {
    private static final Logger LOG = LoggerFactory.getLogger(MatchingResponseGenerator.class);

    private final SoapMessageManager soapMessageManager;
    private final Function<OutboundResponseFromMatchingService, Element> responseElementTransformer;
    private final Function<HealthCheckResponseFromMatchingService, Element> healthCheckResponseTransformer;
    private final ManifestReader manifestReader;
    private final String matchingServiceEntityId;

    public MatchingResponseGenerator(
            SoapMessageManager soapMessageManager,
            Function<OutboundResponseFromMatchingService, Element> responseElementTransformer,
            Function<HealthCheckResponseFromMatchingService, Element> healthCheckResponseTransformer, ManifestReader manifestReader,
            String matchingServiceEntityId) {
        this.soapMessageManager = soapMessageManager;
        this.responseElementTransformer = responseElementTransformer;
        this.healthCheckResponseTransformer = healthCheckResponseTransformer;
        this.manifestReader = manifestReader;
        this.matchingServiceEntityId = matchingServiceEntityId;
    }

    public Response generateResponse(OutboundResponseFromMatchingService outboundResponseFromMatchingService) {
        return ok()
                .entity(soapMessageManager.wrapWithSoapEnvelope(responseElementTransformer.apply(outboundResponseFromMatchingService)))
                .build();
    }

    public Response generateHealthCheckResponse(String requestId) {
        LOG.info("Responding to health check with id '{}'.", requestId);

        String manifestVersionNumber = "UNKNOWN_VERSION_NUMBER";
        try {
            manifestVersionNumber = manifestReader.getAttributeValueFor(MatchingServiceAdapterApplication.class, "Version-Number");
        } catch (IOException e) {
            LOG.error("Failed to read version number from manifest", e);
        }

        HealthCheckMatchingServiceResponse response = new HealthCheckMatchingServiceResponse(new HealthCheckResponseFromMatchingService(
                matchingServiceEntityId,
                requestId,
                manifestVersionNumber
        ));

        return ok()
                .header("ida-msa-version", manifestVersionNumber)
                .entity(soapMessageManager.wrapWithSoapEnvelope(
                                healthCheckResponseTransformer.apply(
                                        response.getHealthCheckResponseFromMatchingService())))
                .build();
    }
}
