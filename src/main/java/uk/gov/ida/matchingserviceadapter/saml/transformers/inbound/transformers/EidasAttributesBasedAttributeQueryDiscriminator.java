package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Issuer;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.repositories.MetadataRepository;

import java.util.Objects;
import java.util.function.Predicate;

public class EidasAttributesBasedAttributeQueryDiscriminator implements Predicate<MatchingServiceRequestContext> {
    private final MetadataRepository countryMetadataRepository;

    public EidasAttributesBasedAttributeQueryDiscriminator(final MetadataRepository countryMetadataRepository) {
        this.countryMetadataRepository = countryMetadataRepository;
    }

    @Override
    public boolean test(MatchingServiceRequestContext matchingServiceRequestContext) {
        return matchingServiceRequestContext.getAssertions().stream()
            .map(Assertion::getIssuer)
            .filter(Objects::nonNull)
            .map(Issuer::getValue)
            .filter(Objects::nonNull)
            .anyMatch(countryMetadataRepository::hasMetadataForEntity);
    }
}
