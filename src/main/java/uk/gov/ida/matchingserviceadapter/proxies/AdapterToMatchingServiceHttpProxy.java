package uk.gov.ida.matchingserviceadapter.proxies;

import com.codahale.metrics.annotation.Timed;
import com.google.inject.Inject;
import com.google.inject.name.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import uk.gov.ida.jerseyclient.JsonClient;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceResponseDto;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.UnknownUserCreationResponseDto;

import java.net.URI;

public class AdapterToMatchingServiceHttpProxy implements AdapterToMatchingServiceProxy {

    private static final Logger LOG = LoggerFactory.getLogger(AdapterToMatchingServiceHttpProxy.class);

    private final JsonClient client;
    private final MatchingServiceAdapterConfiguration configuration;

    @Inject
    public AdapterToMatchingServiceHttpProxy(@Named("MatchingServiceClient") JsonClient client, MatchingServiceAdapterConfiguration configuration) {
        this.client = client;
        this.configuration = configuration;
    }

    @Override
    @Timed
    public MatchingServiceResponseDto makeMatchingServiceRequest(MatchingServiceRequestDto attributeQuery) {
        return client.post(attributeQuery, configuration.getLocalMatchingServiceMatchUrl(), MatchingServiceResponseDto.class);
    }

    @Override
    public UnknownUserCreationResponseDto makeUnknownUserCreationRequest(UnknownUserCreationRequestDto attributeQuery) {
        URI unknownUserCreationServiceUri = configuration.getLocalMatchingServiceAccountCreationUrl();
        return client.post(attributeQuery, unknownUserCreationServiceUri, UnknownUserCreationResponseDto.class);
    }
}
