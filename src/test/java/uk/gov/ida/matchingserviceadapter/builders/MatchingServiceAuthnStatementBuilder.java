package uk.gov.ida.matchingserviceadapter.builders;

import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.MatchingServiceAuthnStatement;

import static uk.gov.ida.saml.core.domain.MatchingServiceAuthnStatement.createIdaAuthnStatement;


public class MatchingServiceAuthnStatementBuilder {

    private AuthnContext levelOfAssurance = AuthnContext.LEVEL_1;

    public static MatchingServiceAuthnStatementBuilder anIdaAuthnStatement() {
        return new MatchingServiceAuthnStatementBuilder();
    }

    public MatchingServiceAuthnStatement build() {
        return createIdaAuthnStatement(levelOfAssurance);
    }
}
