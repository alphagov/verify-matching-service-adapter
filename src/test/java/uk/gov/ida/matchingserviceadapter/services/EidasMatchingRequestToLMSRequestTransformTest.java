package uk.gov.ida.matchingserviceadapter.services;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.hub.domain.LevelOfAssurance;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static uk.gov.ida.saml.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;

@RunWith(OpenSAMLMockitoRunner.class)
public class EidasMatchingRequestToLMSRequestTransformTest {
    private Assertion assertion;

    private EidasMatchingRequestToLMSRequestTransform transform;

    @Before
    public void setUp() {
        assertion = anAssertion()
            .addAuthnStatement(
                anAuthnStatement()
                    .withAuthnContext(anAuthnContext()
                        .withAuthnContextClassRef(
                            anAuthnContextClassRef()
                            .withAuthnContextClasRefValue(LevelOfAssurance.SUBSTANTIAL.toString())
                            .build()
                        ).build()
                    )
                .build()
            ).buildUnencrypted();
        transform = new EidasMatchingRequestToLMSRequestTransform();
    }

    @Test
    public void loaIsMappedCorrectly() {
        // Given
        MatchingServiceRequestContext request = new MatchingServiceRequestContext(null, null, asList(assertion));

        // When
        MatchingServiceRequestDto lmsDto = transform.apply(request);

        // Then
        assertThat(lmsDto.getLevelOfAssurance(), notNullValue());
        assertThat(lmsDto.getLevelOfAssurance().name(), equalTo(LevelOfAssuranceDto.LEVEL_2.name()));

    }

    @Test
    public void testHashedPid() throws Throwable {

    }
}