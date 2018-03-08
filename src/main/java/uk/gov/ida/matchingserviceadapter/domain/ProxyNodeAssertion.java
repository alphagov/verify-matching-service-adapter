package uk.gov.ida.matchingserviceadapter.domain;

import uk.gov.ida.saml.hub.domain.LevelOfAssurance;

public class ProxyNodeAssertion {

    private final String personIdentifier;
    private final LevelOfAssurance levelOfAssurance;
    private final String issuer;

    public ProxyNodeAssertion(LevelOfAssurance levelOfAssurance, String personIdentifier, String issuer) {
        this.personIdentifier = personIdentifier;
        this.levelOfAssurance = levelOfAssurance;
        this.issuer = issuer;
    }

    public LevelOfAssurance getLevelOfAssurance() {
        return levelOfAssurance;
    }

    public String getPersonIdentifier() {
        return personIdentifier;
    }

    public String getIssuer() {
        return issuer;
    }
}
