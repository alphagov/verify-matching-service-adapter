package uk.gov.ida.matchingserviceadapter.resources;

import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ida.matchingserviceadapter.exceptions.FederationMetadataLoadingException;
import uk.gov.ida.matchingserviceadapter.repositories.MatchingServiceAdapterMetadataRepository;

import javax.xml.parsers.ParserConfigurationException;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@RunWith(MockitoJUnitRunner.class)
public class LocalMetadataResourceTest {

    @Mock
    private MatchingServiceAdapterMetadataRepository metadataRepository;

    LocalMetadataResource localMetadataResource;

    @Before
    public void setUp() {
        localMetadataResource = new LocalMetadataResource(metadataRepository);
    }

    @Test
    public void testMetadataRetreival() throws FederationMetadataLoadingException, ResolverException, ParserConfigurationException {
        localMetadataResource.getMetadata();

        verify(metadataRepository, times(1)).getMatchingServiceAdapterMetadata();
    }

}