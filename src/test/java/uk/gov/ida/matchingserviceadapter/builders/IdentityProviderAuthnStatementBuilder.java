package uk.gov.ida.matchingserviceadapter.builders;

import com.google.common.base.Optional;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.FraudAuthnDetails;
import uk.gov.ida.saml.core.domain.IdentityProviderAuthnStatement;
import uk.gov.ida.saml.core.domain.IpAddress;

import static com.google.common.base.Optional.fromNullable;
import static uk.gov.ida.saml.core.domain.IdentityProviderAuthnStatement.createIdentityProviderAuthnStatement;
import static uk.gov.ida.saml.core.domain.IdentityProviderAuthnStatement.createIdentityProviderFraudAuthnStatement;

public class IdentityProviderAuthnStatementBuilder {

    private Optional<FraudAuthnDetails> fraudAuthnDetails = Optional.absent();
    private AuthnContext authnContext = AuthnContext.LEVEL_1;
    private Optional<IpAddress> userIpAddress = fromNullable(IpAddressBuilder.anIpAddress().build());

    public static IdentityProviderAuthnStatementBuilder anIdentityProviderAuthnStatement() {
        return new IdentityProviderAuthnStatementBuilder();
    }

    public IdentityProviderAuthnStatement build() {
        if (fraudAuthnDetails.isPresent()) {
            return createIdentityProviderFraudAuthnStatement(fraudAuthnDetails.get(), userIpAddress.orNull());
        }
        return createIdentityProviderAuthnStatement(authnContext, userIpAddress.orNull());
    }

    public IdentityProviderAuthnStatementBuilder withAuthnContext(AuthnContext authnContext) {
        this.authnContext = authnContext;
        return this;
    }

    public IdentityProviderAuthnStatementBuilder withFraudDetails(FraudAuthnDetails fraudDetails) {
        this.fraudAuthnDetails = Optional.fromNullable(fraudDetails);
        return this;
    }

    public IdentityProviderAuthnStatementBuilder withUserIpAddress(IpAddress userIpAddress) {
        this.userIpAddress = fromNullable(userIpAddress);
        return this;
    }
}
