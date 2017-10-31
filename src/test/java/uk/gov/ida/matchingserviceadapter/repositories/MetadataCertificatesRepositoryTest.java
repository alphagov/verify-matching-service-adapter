package uk.gov.ida.matchingserviceadapter.repositories;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import org.opensaml.saml.saml2.metadata.EntityDescriptor;
import uk.gov.ida.common.shared.security.Certificate;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.builders.CertificateBuilder.aCertificate;

@RunWith(MockitoJUnitRunner.class)
public class MetadataCertificatesRepositoryTest {

    public static String HUB_ID = "hub-id";

    @Mock
    private MetadataResolver metadataResolver;
    @Mock
    private CertificateValidator certificateValidator;
    @Mock
    private CertificateExtractor extractor;

    private MetadataCertificatesRepository repository;

    @Before
    public void setUp() throws Exception {
        repository = new MetadataCertificatesRepository(
                metadataResolver,
                certificateValidator,
                extractor);
    }

    @Test
    public void getIdpSigningCertificates_shouldFetchAndValidateCertificates() throws ResolverException {
        String entityId = "entity-id";

        EntityDescriptor entityDescriptor = mock(EntityDescriptor.class);
        when(metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(entityId)))).thenReturn(entityDescriptor);

        List<Certificate> certificates = Arrays.asList(aCertificate().build());
        when(extractor.extractIdpSigningCertificates(entityDescriptor)).thenReturn(certificates);

        Collection<Certificate> actualCertificates = repository.getIdpSigningCertificates(entityId);

        assertThat(actualCertificates).isEqualTo(certificates);
        verify(certificateValidator).validate(certificates);
    }

    @Test
    public void getHubSigningCertificates_shouldFetchAndValidateCertificates() throws ResolverException {
        EntityDescriptor entityDescriptor = mock(EntityDescriptor.class);
        when(metadataResolver.resolveSingle(new CriteriaSet(new EntityIdCriterion(HUB_ID)))).thenReturn(entityDescriptor);

        List<Certificate> certificates = Arrays.asList(aCertificate().build());
        when(extractor.extractHubSigningCertificates(entityDescriptor)).thenReturn(certificates);

        Collection<Certificate> actualCertificates = repository.getHubSigningCertificates(HUB_ID);

        assertThat(actualCertificates).isEqualTo(certificates);
        verify(certificateValidator).validate(certificates);
    }

}
