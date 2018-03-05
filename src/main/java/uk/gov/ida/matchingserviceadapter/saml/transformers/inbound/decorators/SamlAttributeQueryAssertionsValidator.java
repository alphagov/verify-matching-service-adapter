package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.decorators;

import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.matchingserviceadapter.MatchingServiceAdapterConfiguration;
import uk.gov.ida.matchingserviceadapter.saml.security.ValidatedAttributeQuery;
import uk.gov.ida.saml.core.validators.assertion.AssertionValidator;
import uk.gov.ida.saml.core.validators.assertion.IdentityProviderAssertionValidator;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

import static uk.gov.ida.matchingserviceadapter.binders.MatchingServiceAdapterSamlBinder.HUB_ENTITY_ID;

public class SamlAttributeQueryAssertionsValidator {

    private final AssertionValidator assertionValidator;
    private final IdentityProviderAssertionValidator identityProviderAssertionValidator;
    private final MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration;
    private final String hubEntityId;

    @Inject
    public SamlAttributeQueryAssertionsValidator(
            AssertionValidator assertionValidator,
            IdentityProviderAssertionValidator identityProviderAssertionValidator,
            MatchingServiceAdapterConfiguration matchingServiceAdapterConfiguration,
            @Named(HUB_ENTITY_ID) String hubEntityId) {

        this.assertionValidator = assertionValidator;
        this.identityProviderAssertionValidator = identityProviderAssertionValidator;
        this.matchingServiceAdapterConfiguration = matchingServiceAdapterConfiguration;
        this.hubEntityId = hubEntityId;
    }

    public void validateHubAssertions(ValidatedAttributeQuery attributeQuery, List<Assertion> assertions) {
        for (Assertion assertion : assertions) {
            assertionValidator.validate(
                    assertion,
                    attributeQuery.getID(),
                    matchingServiceAdapterConfiguration.getEntityId());
        }
    }

    public void validateIdpAssertions(ValidatedAttributeQuery attributeQuery, List<Assertion> assertions) {
        for (Assertion assertion : assertions) {
            identityProviderAssertionValidator.validate(
                    assertion,
                    attributeQuery.getID(),
                    hubEntityId);
        }
        identityProviderAssertionValidator.validateConsistency(assertions);
    }
}
