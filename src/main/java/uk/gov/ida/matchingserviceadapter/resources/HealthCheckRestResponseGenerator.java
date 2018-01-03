package uk.gov.ida.matchingserviceadapter.resources;

import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.utils.manifest.ManifestReader;

import javax.ws.rs.core.Response;
import java.util.function.Function;

import static javax.ws.rs.core.Response.ok;

public class HealthCheckRestResponseGenerator implements MatchingServiceRestResponseGenerator<HealthCheckMatchingServiceResponse> {

    private final SoapMessageManager soapMessageManager;
    private final Function<HealthCheckResponseFromMatchingService, Element> healthCheckResponseTransformer;
    private final ManifestReader manifestReader;

    public HealthCheckRestResponseGenerator(SoapMessageManager soapMessageManager, Function<HealthCheckResponseFromMatchingService, Element> healthCheckResponseTransformer, ManifestReader manifestReader) {
        this.soapMessageManager = soapMessageManager;
        this.healthCheckResponseTransformer = healthCheckResponseTransformer;
        this.manifestReader = manifestReader;
    }

    @Override
    public Response generateResponse(HealthCheckMatchingServiceResponse healthCheckMatchingServiceResponse) {
        return ok()
            .header("ida-msa-version", manifestReader.getValue("Version-Number"))
            .entity(
                soapMessageManager.wrapWithSoapEnvelope(
                    healthCheckResponseTransformer.apply(
                        healthCheckMatchingServiceResponse.getHealthCheckResponseFromMatchingService())))
            .build();
    }
}
