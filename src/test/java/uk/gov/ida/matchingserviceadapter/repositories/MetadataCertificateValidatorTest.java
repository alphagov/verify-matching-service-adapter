package uk.gov.ida.matchingserviceadapter.repositories;

import net.shibboleth.utilities.java.support.resolver.CriteriaSet;
import net.shibboleth.utilities.java.support.resolver.ResolverException;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opensaml.core.criterion.EntityIdCriterion;
import org.opensaml.saml.metadata.resolver.MetadataResolver;
import uk.gov.ida.matchingserviceadapter.exceptions.InvalidSamlMetadataException;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.test.TestEntityIds;
import uk.gov.ida.saml.metadata.test.factories.metadata.EntityDescriptorFactory;

import java.util.List;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(OpenSAMLMockitoRunner.class)
public class MetadataCertificateValidatorTest {

    private final String HUB_FEDERATION_ID = "hub-federation";

    @Mock
    MetadataResolver resolver;
    @Mock
    MetadataCertificatesRepository repository;

    @Test(expected = InvalidSamlMetadataException.class)
    public void validateShouldThrowWhenMetadataIsInvalid() throws ResolverException {
        when(resolver.resolve(new CriteriaSet(new EntityIdCriterion(HUB_FEDERATION_ID)))).thenThrow(ResolverException.class);
        new MetadataCertificateValidator(resolver, repository, TestEntityIds.HUB_ENTITY_ID, HUB_FEDERATION_ID).validateAll();
    }

    @Test
    public void validateShouldNotThrowWhenMetadataIsValid() throws ResolverException {
        List entitiesDescriptor = new EntityDescriptorFactory().defaultEntityDescriptors();

        when(resolver.resolve(new CriteriaSet(new EntityIdCriterion(HUB_FEDERATION_ID)))).thenReturn(entitiesDescriptor);
        new MetadataCertificateValidator(resolver, repository, TestEntityIds.HUB_ENTITY_ID, HUB_FEDERATION_ID).validateAll();
        verify(repository).getHubEncryptionCertificates(TestEntityIds.HUB_ENTITY_ID);
        verify(repository).getHubSigningCertificates(TestEntityIds.HUB_ENTITY_ID);
        verify(repository).getIdpSigningCertificates(TestEntityIds.STUB_IDP_ONE);
        verify(repository).getIdpSigningCertificates(TestEntityIds.STUB_IDP_TWO);
        verify(repository).getIdpSigningCertificates(TestEntityIds.STUB_IDP_THREE);
        verify(repository).getIdpSigningCertificates(TestEntityIds.STUB_IDP_FOUR);
    }
}