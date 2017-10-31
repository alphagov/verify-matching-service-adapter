package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers;

import com.google.inject.Inject;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;

import java.util.function.Function;
import java.util.function.Predicate;

public class DiscriminatingAttributeQueryToInboundMSRequestTransformer implements Function<AttributeQuery, InboundMatchingServiceRequest> {
    private final Predicate<AttributeQuery> eidasAttributeQueryDiscriminator;
    private final Function<AttributeQuery, InboundMatchingServiceRequest> verifyTransformer;
    private final Function<AttributeQuery, InboundMatchingServiceRequest> eidasTransformer;

    @Inject
    public DiscriminatingAttributeQueryToInboundMSRequestTransformer(
        final Predicate<AttributeQuery> eidasAttributeQueryDiscriminator,
        final Function<AttributeQuery, InboundMatchingServiceRequest> verifyTransformer,
        final Function<AttributeQuery, InboundMatchingServiceRequest> eidasTransformer) {

        this.eidasAttributeQueryDiscriminator = eidasAttributeQueryDiscriminator;
        this.verifyTransformer = verifyTransformer;
        this.eidasTransformer = eidasTransformer;
    }

    @Override
    public InboundMatchingServiceRequest apply(AttributeQuery attributeQuery) {
        if ( eidasAttributeQueryDiscriminator.test(attributeQuery) ) {
            return eidasTransformer.apply(attributeQuery);
        } else {
            return verifyTransformer.apply(attributeQuery);
        }
    }
}
