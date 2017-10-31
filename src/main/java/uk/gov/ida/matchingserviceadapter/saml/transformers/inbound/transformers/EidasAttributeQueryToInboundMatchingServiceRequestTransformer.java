package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers;

import com.google.inject.Inject;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.saml.security.AttributeQuerySignatureValidator;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.decorators.SamlAttributeQueryValidator;

import java.util.function.Function;


public class EidasAttributeQueryToInboundMatchingServiceRequestTransformer implements Function<AttributeQuery, InboundMatchingServiceRequest> {
    private final SamlAttributeQueryValidator samlAttributeQueryValidator;
    private final AttributeQuerySignatureValidator attributeQuerySignatureValidator;

    @Inject
    public EidasAttributeQueryToInboundMatchingServiceRequestTransformer(
        final SamlAttributeQueryValidator samlAttributeQueryValidator,
        final AttributeQuerySignatureValidator attributeQuerySignatureValidator
    ) {
        this.samlAttributeQueryValidator = samlAttributeQueryValidator;
        this.attributeQuerySignatureValidator = attributeQuerySignatureValidator;
    }

    @Override
    public InboundMatchingServiceRequest apply(AttributeQuery attributeQuery) {
        samlAttributeQueryValidator.validate(attributeQuery);
        attributeQuerySignatureValidator.validate(attributeQuery);

        // TODO - EID-XXX handle attributes and fill out the nulls below
        throw new RuntimeException("TODO: Signature valid. Next stage of eIDAS MSA development is to validate the AQR");
    }
}
