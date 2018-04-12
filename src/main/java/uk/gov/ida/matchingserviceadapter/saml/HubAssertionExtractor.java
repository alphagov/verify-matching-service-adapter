package uk.gov.ida.matchingserviceadapter.saml; // TODO: where should this go?

import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import javax.inject.Inject;
import javax.inject.Named;

import org.opensaml.saml.saml2.core.Assertion;

import uk.gov.ida.saml.core.domain.HubAssertion;
import uk.gov.ida.saml.core.transformers.inbound.HubAssertionUnmarshaller;

public class HubAssertionExtractor {
    private final String hubEntityId;
    private final HubAssertionUnmarshaller hubAssertionUnmarshaller;

    @Inject
    public HubAssertionExtractor(
        @Named("HubEntityId") String hubEntityId,
        HubAssertionUnmarshaller hubAssertionUnmarshaller) {
        this.hubEntityId = hubEntityId;
        this.hubAssertionUnmarshaller = hubAssertionUnmarshaller;
    }

    public List<Assertion> getHubAssertions(List<Assertion> assertions) {
        return assertions.stream()
            .filter(this::isHubAssertion)
            .collect(Collectors.toList());
    }

    public Optional<HubAssertion> getHubAssertion(List<Assertion> assertions) {
        return getHubAssertions(assertions).stream()
            .findFirst()
            .map(hubAssertionUnmarshaller::toHubAssertion);
    }

    public List<Assertion> getNonHubAssertions(List<Assertion> assertions) {
        return assertions.stream()
            .filter(((Predicate<Assertion>)this::isHubAssertion).negate())
            .collect(Collectors.toList());
    }

    public boolean isHubAssertion(final Assertion assertion) {
          return assertion.getIssuer().getValue().equals(hubEntityId);
    }
};