package uk.gov.ida.matchingserviceadapter.domain;

import org.opensaml.saml.saml2.core.Attribute;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;

import java.util.List;

public class TranslatedAttributeQueryRequest {
    private final MatchingServiceRequestDto matchingServiceRequestDto;
    private final String issuer;
    private final String assertionConsumerServiceUrl;
    private final String authnRequestIssuerId;
    private final List<Attribute> userAccountCreationAttributes;
    private boolean isHealthCheck;

    public TranslatedAttributeQueryRequest(MatchingServiceRequestDto matchingServiceRequestDto, String issuer, String assertionConsumerServiceUrl, String authnRequestIssuerId, List<Attribute> userAccountCreationAttributes, boolean isHealthCheck) {
        this.matchingServiceRequestDto = matchingServiceRequestDto;
        this.issuer = issuer;
        this.assertionConsumerServiceUrl = assertionConsumerServiceUrl;
        this.authnRequestIssuerId = authnRequestIssuerId;
        this.userAccountCreationAttributes = userAccountCreationAttributes;
        this.isHealthCheck = isHealthCheck;
    }

    public MatchingServiceRequestDto getMatchingServiceRequestDto() {
        return matchingServiceRequestDto;
    }

    public String getIssuer() {
        return issuer;
    }

    public String getAssertionConsumerServiceUrl() {
        return assertionConsumerServiceUrl;
    }

    public String getAuthnRequestIssuerId() {
        return authnRequestIssuerId;
    }

    public List<Attribute> getUserAccountCreationAttributes() {
        return userAccountCreationAttributes;
    }

    public boolean isHealthCheck() {
        return isHealthCheck;
    }
}
