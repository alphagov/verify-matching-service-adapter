package uk.gov.ida.matchingserviceadapter.resources;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterApplication;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.HealthCheckResponseFromMatchingService;
import uk.gov.ida.shared.utils.manifest.ManifestReader;

import javax.ws.rs.core.Response;
import java.io.IOException;
import java.util.function.Function;

import static javax.ws.rs.core.Response.ok;

public class HealthCheckResponseGenerator implements MatchingServiceResponseGenerator<HealthCheckMatchingServiceResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(HealthCheckResponseGenerator.class);


    private final SoapMessageManager soapMessageManager;
    private final Function<HealthCheckResponseFromMatchingService, Element> healthCheckResponseTransformer;
    private final ManifestReader manifestReader;

    public HealthCheckResponseGenerator(
        SoapMessageManager soapMessageManager,
        Function<HealthCheckResponseFromMatchingService, Element> healthCheckResponseTransformer,
        ManifestReader manifestReader
    ) {
        this.soapMessageManager = soapMessageManager;
        this.healthCheckResponseTransformer = healthCheckResponseTransformer;
        this.manifestReader = manifestReader;
    }

    @Override
    public Response generateResponse(HealthCheckMatchingServiceResponse healthCheckMatchingServiceResponse) {
        String manifestVersionNumber = "UNKNOWN_VERSION_NUMBER";
        try {
            manifestVersionNumber = manifestReader.getAttributeValueFor(MatchingServiceAdapterApplication.class, "Version-Number");
        } catch (IOException e) {
            LOG.error("Failed to read version number from manifest", e);
        }

        return ok()
            .header("ida-msa-version", manifestVersionNumber)
            .entity(
                soapMessageManager.wrapWithSoapEnvelope(
                    healthCheckResponseTransformer.apply(
                        healthCheckMatchingServiceResponse.getHealthCheckResponseFromMatchingService())))
            .build();
    }
}
