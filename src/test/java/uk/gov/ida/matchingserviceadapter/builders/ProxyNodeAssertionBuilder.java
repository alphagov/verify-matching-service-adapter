package uk.gov.ida.matchingserviceadapter.builders;

import uk.gov.ida.matchingserviceadapter.domain.EidasMatchingDataset;
import uk.gov.ida.matchingserviceadapter.domain.ProxyNodeAssertion;
import uk.gov.ida.saml.hub.domain.LevelOfAssurance;

public class ProxyNodeAssertionBuilder {

    private String personIdentifier;
    private LevelOfAssurance levelOfAssurance;
    private String issuer;
    private EidasMatchingDataset eidasMatchingDataset;

    public static ProxyNodeAssertionBuilder anProxyNodeAssertion() {
        return new ProxyNodeAssertionBuilder();
    }

    public ProxyNodeAssertion build() {
        return new ProxyNodeAssertion(
                levelOfAssurance,
                personIdentifier,
                issuer,
                eidasMatchingDataset);
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

    public ProxyNodeAssertionBuilder withEidasMatchingDataset(EidasMatchingDataset eidasMatchingDataset) {
        this.eidasMatchingDataset = eidasMatchingDataset;
        return this;
    }
}
