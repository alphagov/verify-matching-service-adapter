package uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.AuthnContext;
import org.opensaml.saml.saml2.core.AuthnStatement;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.domain.MatchingServiceAuthnStatement;

import javax.inject.Inject;

public class MatchingServiceAuthnStatementToAuthnStatementTransformer {

    @Inject
    public MatchingServiceAuthnStatementToAuthnStatementTransformer(
            final OpenSamlXmlObjectFactory openSamlXmlObjectFactory) {

        this.openSamlXmlObjectFactory = openSamlXmlObjectFactory;
    }

    private final OpenSamlXmlObjectFactory openSamlXmlObjectFactory;

    public AuthnStatement transform(MatchingServiceAuthnStatement idaAuthnStatement) {
        AuthnStatement authnStatement = openSamlXmlObjectFactory.createAuthnStatement();
        AuthnContext authnContext = openSamlXmlObjectFactory.createAuthnContext();
        authnContext.setAuthnContextClassRef(openSamlXmlObjectFactory.createAuthnContextClassReference(idaAuthnStatement.getAuthnContext().getUri()));
        authnStatement.setAuthnContext(authnContext);
        authnStatement.setAuthnInstant(DateTime.now());
        return authnStatement;
    }
}
