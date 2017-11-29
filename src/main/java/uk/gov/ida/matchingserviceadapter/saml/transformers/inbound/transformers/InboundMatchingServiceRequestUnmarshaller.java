package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers;

import com.google.common.base.Optional;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Subject;
import uk.gov.ida.matchingserviceadapter.saml.security.ValidatedAttributeQuery;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundMatchingServiceRequest;
import uk.gov.ida.saml.core.domain.HubAssertion;
import uk.gov.ida.saml.core.domain.IdentityProviderAssertion;
import uk.gov.ida.saml.core.transformers.IdentityProviderAssertionUnmarshaller;
import uk.gov.ida.saml.core.transformers.inbound.HubAssertionUnmarshaller;
import uk.gov.ida.saml.security.validators.ValidatedAssertions;

public class InboundMatchingServiceRequestUnmarshaller {

    private final HubAssertionUnmarshaller hubAssertionUnmarshaller;
    private final IdentityProviderAssertionUnmarshaller identityProviderAssertionUnmarshaller;

    public InboundMatchingServiceRequestUnmarshaller(
            HubAssertionUnmarshaller hubAssertionUnmarshaller,
            IdentityProviderAssertionUnmarshaller identityProviderAssertionUnmarshaller) {

        this.hubAssertionUnmarshaller = hubAssertionUnmarshaller;
        this.identityProviderAssertionUnmarshaller = identityProviderAssertionUnmarshaller;
    }

    @SuppressWarnings("unchecked") // we know this cast will work
    public InboundMatchingServiceRequest fromSaml(ValidatedAttributeQuery originalQuery, ValidatedAssertions validatedHubAssertions, ValidatedAssertions validatedIdpAssertions) {
        String id = originalQuery.getID();
        String originalIssuer = originalQuery.getIssuer().getValue();
        Subject subject = originalQuery.getSubject();

        IdentityProviderAssertion matchingDatasetAssertion = null;
        IdentityProviderAssertion authnStatementAssertion = null;
        Optional<HubAssertion> cycle3AttributeAssertion = Optional.absent();

        for (Assertion assertion : validatedHubAssertions.getAssertions()) {
            HubAssertion hubAssertion = hubAssertionUnmarshaller.toHubAssertion(assertion);
            if (hubAssertion.getCycle3Data().isPresent()) {
                cycle3AttributeAssertion = Optional.of(hubAssertion);
            }
        }
        for (Assertion assertion : validatedIdpAssertions.getAssertions()) {
            IdentityProviderAssertion identityProviderAssertion = identityProviderAssertionUnmarshaller.fromAssertion(assertion);
            if (identityProviderAssertion.getMatchingDataset().isPresent()) {
                matchingDatasetAssertion = identityProviderAssertion;
            }
            if (identityProviderAssertion.getAuthnStatement().isPresent()) {
                authnStatementAssertion = identityProviderAssertion;
            }
        }

        String authnRequestIssuerId = subject.getNameID().getSPNameQualifier();
        String assertionConsumerUrl = subject.getNameID().getNameQualifier();

        return new InboundMatchingServiceRequest(
                id,
                originalIssuer,
                matchingDatasetAssertion,
                authnStatementAssertion,
                cycle3AttributeAssertion,
                originalQuery.getIssueInstant(),
                authnRequestIssuerId,
                assertionConsumerUrl,
                originalQuery.getAttributes()
        );
    }

}
