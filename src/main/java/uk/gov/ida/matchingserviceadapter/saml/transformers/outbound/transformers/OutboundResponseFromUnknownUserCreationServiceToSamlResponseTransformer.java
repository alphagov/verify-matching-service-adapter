package uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers;

import com.google.common.base.Optional;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Response;
import org.opensaml.saml.saml2.core.Status;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.OutboundResponseFromUnknownUserCreationService;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.transformers.outbound.IdaResponseToSamlResponseTransformer;
import uk.gov.ida.saml.hub.transformers.outbound.UnknownUserCreationIdaStatusMarshaller;

public class OutboundResponseFromUnknownUserCreationServiceToSamlResponseTransformer extends IdaResponseToSamlResponseTransformer<OutboundResponseFromUnknownUserCreationService> {

    private final UnknownUserCreationIdaStatusMarshaller statusMarshaller;
    private final MatchingServiceAssertionToAssertionTransformer assertionTransformer;

    public OutboundResponseFromUnknownUserCreationServiceToSamlResponseTransformer(
            OpenSamlXmlObjectFactory openSamlXmlObjectFactory,
            UnknownUserCreationIdaStatusMarshaller statusMarshaller,
            MatchingServiceAssertionToAssertionTransformer assertionTransformer) {
        super(openSamlXmlObjectFactory);
        this.statusMarshaller = statusMarshaller;
        this.assertionTransformer = assertionTransformer;
    }

    @Override
    protected void transformAssertions(OutboundResponseFromUnknownUserCreationService originalResponse, Response transformedResponse) {
        Optional<MatchingServiceAssertion> assertion = originalResponse.getMatchingServiceAssertion();
        if (assertion.isPresent()) {
            Assertion transformedAssertion = assertionTransformer.apply(assertion.get());
            transformedResponse.getAssertions().add(transformedAssertion);
        }
    }

    @Override
    protected Status transformStatus(OutboundResponseFromUnknownUserCreationService originalResponse) {
        return statusMarshaller.toSamlStatus(originalResponse.getStatus());
    }

    @Override
    protected void transformDestination(OutboundResponseFromUnknownUserCreationService originalResponse, Response transformedResponse) {
        // this method intentionally left blank
    }
}
