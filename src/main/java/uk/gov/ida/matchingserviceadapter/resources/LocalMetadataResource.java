package uk.gov.ida.matchingserviceadapter.resources;

import com.google.inject.Inject;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.w3c.dom.Document;
import uk.gov.ida.Constants;
import uk.gov.ida.matchingserviceadapter.exceptions.FederationMetadataLoadingException;
import uk.gov.ida.matchingserviceadapter.repositories.MatchingServiceAdapterMetadataRepository;
import uk.gov.ida.matchingserviceadapter.rest.Urls;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

@Path(Urls.MatchingServiceAdapterUrls.MATCHING_SERVICE_METADATA_RESOURCE)
@Produces(MediaType.TEXT_XML)
public class LocalMetadataResource {

    private final MatchingServiceAdapterMetadataRepository metadataRepository;

    @Inject
    public LocalMetadataResource(MatchingServiceAdapterMetadataRepository metadataRepository) {

        this.metadataRepository = metadataRepository;
    }

    @GET
    @Produces(Constants.APPLICATION_SAMLMETADATA_XML)
    public Document getMetadata() throws ResolverException, FederationMetadataLoadingException {
        return metadataRepository.getMatchingServiceAdapterMetadata();
    }
}
