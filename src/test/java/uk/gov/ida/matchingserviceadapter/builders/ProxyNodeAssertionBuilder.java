package uk.gov.ida.matchingserviceadapter.builders;

import uk.gov.ida.matchingserviceadapter.domain.ProxyNodeAssertion;
import uk.gov.ida.saml.hub.domain.LevelOfAssurance;

public class ProxyNodeAssertionBuilder {

    private String personIdentifier;
    private LevelOfAssurance levelOfAssurance;
    private String issuer;

    public static ProxyNodeAssertionBuilder anProxyNodeAssertion() {
        return new ProxyNodeAssertionBuilder();
    }

    public ProxyNodeAssertion build() {
        return new ProxyNodeAssertion(
                levelOfAssurance,
                personIdentifier,
                issuer
        );
    }

    public ProxyNodeAssertionBuilder withPersonIdentifier(String personIdentifier) {
        this.personIdentifier = personIdentifier;
        return this;
    }

    public ProxyNodeAssertionBuilder withLevelOfAssurance(LevelOfAssurance levelOfAssurance) {
        this.levelOfAssurance = levelOfAssurance;
        return this;
    }

    public ProxyNodeAssertionBuilder withIssuer(String issuer) {
        this.issuer = issuer;
        return this;
    }
}
