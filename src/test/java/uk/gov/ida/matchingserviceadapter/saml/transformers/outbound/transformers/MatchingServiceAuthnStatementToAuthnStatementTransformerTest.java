package uk.gov.ida.matchingserviceadapter.saml.transformers.outbound.transformers;

import org.assertj.core.api.Assertions;
import org.joda.time.DateTime;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.AuthnStatement;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;
import uk.gov.ida.saml.core.domain.AuthnContext;
import uk.gov.ida.saml.core.domain.MatchingServiceAuthnStatement;
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.test.OpenSAMLRunner;
import uk.gov.ida.shared.utils.datetime.DateTimeFreezer;

import static org.assertj.jodatime.api.Assertions.assertThat;

@RunWith(OpenSAMLRunner.class)
public class MatchingServiceAuthnStatementToAuthnStatementTransformerTest {

    private MatchingServiceAuthnStatementToAuthnStatementTransformer transformer;

    @Before
    public void setup() {
        DateTimeFreezer.freezeTime();
        transformer = new MatchingServiceAuthnStatementToAuthnStatementTransformer(new OpenSamlXmlObjectFactory());
    }

    @Test
    public void shouldTransformAuthnStatementWithLevel1() throws Exception {
        verifyLevel(AuthnContext.LEVEL_1, IdaAuthnContext.LEVEL_1_AUTHN_CTX);
    }

    @Test
    public void shouldTransformAuthnStatementWithLevel2() throws Exception {
        verifyLevel(AuthnContext.LEVEL_2, IdaAuthnContext.LEVEL_2_AUTHN_CTX);
    }

    @Test
    public void shouldTransformAuthnStatementWithLevel3() throws Exception {
        verifyLevel(AuthnContext.LEVEL_3, IdaAuthnContext.LEVEL_3_AUTHN_CTX);
    }

    @Test
    public void shouldTransformAuthnStatementWithLevel4() throws Exception {
        verifyLevel(AuthnContext.LEVEL_4, IdaAuthnContext.LEVEL_4_AUTHN_CTX);
    }

    @Test
    public void shouldTransformAuthnStatementWithLevelX() throws Exception {
        verifyLevel(AuthnContext.LEVEL_X, IdaAuthnContext.LEVEL_X_AUTHN_CTX);
    }

    private void verifyLevel(AuthnContext requestedLevel, String expectedLevel) {
        MatchingServiceAuthnStatement matchingServiceAuthnStatement = MatchingServiceAuthnStatement.createIdaAuthnStatement(requestedLevel);

        AuthnStatement authnStatement = transformer.transform(matchingServiceAuthnStatement);

        assertThat(authnStatement.getAuthnInstant()).isEqualTo(DateTime.now());
        Assertions.assertThat(authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef()).isEqualTo(expectedLevel);
    }

    @After
    public void after(){
        DateTimeFreezer.unfreezeTime();
    }
}
