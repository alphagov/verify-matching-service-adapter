package uk.gov.ida.matchingserviceadapter.resources;

import org.w3c.dom.Element;
import uk.gov.ida.matchingserviceadapter.domain.HealthCheckMatchingServiceResponse;
import uk.gov.ida.matchingserviceadapter.rest.soap.SoapMessageManager;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.HealthCheckResponseFromMatchingService;
import uk.gov.ida.matchingserviceadapter.utils.manifest.ManifestReader;

import javax.ws.rs.core.Response;
import java.util.function.Function;

import static javax.ws.rs.core.Response.ok;

/**
 * Created by gary on 17/11/17.
 */
public class HealthCheckResponseRenderer implements MatchingServiceResponseRenderer<HealthCheckMatchingServiceResponse> {

    private final SoapMessageManager soapMessageManager;
    private final Function<HealthCheckResponseFromMatchingService, Element> healthCheckResponseTransformer;
    private final ManifestReader manifestReader;

    public HealthCheckResponseRenderer(SoapMessageManager soapMessageManager, Function<HealthCheckResponseFromMatchingService, Element> healthCheckResponseTransformer, ManifestReader manifestReader) {
        this.soapMessageManager = soapMessageManager;
        this.healthCheckResponseTransformer = healthCheckResponseTransformer;
        this.manifestReader = manifestReader;
    }

    @Override
    public Response render(HealthCheckMatchingServiceResponse healthCheckMatchingServiceResponse) {
        return ok()
            .header("ida-msa-version", manifestReader.getManifest().getValue("Version-Number"))
            .entity(
                soapMessageManager.wrapWithSoapEnvelope(
                    healthCheckResponseTransformer.apply(
                        healthCheckMatchingServiceResponse.getHealthCheckResponseFromMatchingService())))
            .build();
    }
}
