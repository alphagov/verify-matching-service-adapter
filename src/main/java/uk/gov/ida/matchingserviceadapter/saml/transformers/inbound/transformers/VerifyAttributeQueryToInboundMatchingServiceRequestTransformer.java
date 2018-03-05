package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers;

import com.google.inject.Inject;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import uk.gov.ida.matchingserviceadapter.saml.security.AttributeQuerySignatureValidator;
import uk.gov.ida.matchingserviceadapter.saml.security.ValidatedAttributeQuery;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.decorators.SamlAttributeQueryAssertionsValidator;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.decorators.SamlAttributeQueryValidator;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.validators.ValidatedAssertions;

import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import static uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterSamlBinder.HUB_ENTITY_ID;

public class VerifyAttributeQueryToInboundMatchingServiceRequestTransformer implements Function<AttributeQuery, InboundMatchingServiceRequest> {

    private final SamlAttributeQueryValidator samlAttributeQueryValidator;
    private final AttributeQuerySignatureValidator attributeQuerySignatureValidator;
    private final SamlAssertionsSignatureValidator samlAssertionsSignatureValidator;
    private final InboundMatchingServiceRequestUnmarshaller inboundMatchingServiceRequestUnmarshaller;
    private final SamlAttributeQueryAssertionsValidator samlAttributeQueryAssertionsValidator;
    private final AssertionDecrypter assertionDecrypter;
    private final String hubEntityId;

    @Inject
    public VerifyAttributeQueryToInboundMatchingServiceRequestTransformer(
            final SamlAttributeQueryValidator samlAttributeQueryValidator,
            final AttributeQuerySignatureValidator attributeQuerySignatureValidator,
            final SamlAssertionsSignatureValidator samlAssertionsSignatureValidator,
            final InboundMatchingServiceRequestUnmarshaller inboundMatchingServiceRequestUnmarshaller,
            final SamlAttributeQueryAssertionsValidator samlAttributeQueryAssertionsValidator,
            final AssertionDecrypter assertionDecrypter,
            @Named(HUB_ENTITY_ID) final String hubEntityId) {

        this.samlAttributeQueryValidator = samlAttributeQueryValidator;
        this.attributeQuerySignatureValidator = attributeQuerySignatureValidator;
        this.samlAssertionsSignatureValidator = samlAssertionsSignatureValidator;
        this.inboundMatchingServiceRequestUnmarshaller = inboundMatchingServiceRequestUnmarshaller;
        this.samlAttributeQueryAssertionsValidator = samlAttributeQueryAssertionsValidator;
        this.assertionDecrypter = assertionDecrypter;
        this.hubEntityId = hubEntityId;
    }

    public InboundMatchingServiceRequest apply(final AttributeQuery attributeQuery) {
        samlAttributeQueryValidator.validate(attributeQuery);
        ValidatedAttributeQuery validatedAttributeQuery = attributeQuerySignatureValidator.validate(attributeQuery);

        List<Assertion> assertions = assertionDecrypter.decryptAssertions(validatedAttributeQuery);

        Map<Boolean, List<Assertion>> map = assertions.stream().collect(Collectors.groupingBy(this::isHubAssertion));
        List<Assertion> hubAssertions = map.getOrDefault(true, Collections.emptyList());
        List<Assertion> idpAssertions = map.getOrDefault(false, Collections.emptyList());

        samlAttributeQueryAssertionsValidator.validateHubAssertions(validatedAttributeQuery, hubAssertions);
        samlAttributeQueryAssertionsValidator.validateIdpAssertions(validatedAttributeQuery, idpAssertions);

        ValidatedAssertions validatedHubAssertions = samlAssertionsSignatureValidator.validate(hubAssertions, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        ValidatedAssertions validatedIdpAssertions = samlAssertionsSignatureValidator.validate(idpAssertions, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        return inboundMatchingServiceRequestUnmarshaller.fromSaml(validatedAttributeQuery, validatedHubAssertions, validatedIdpAssertions);
    }

    private boolean isHubAssertion(Assertion assertion) {
        return assertion.getIssuer().getValue().equals(hubEntityId);
    }

}
