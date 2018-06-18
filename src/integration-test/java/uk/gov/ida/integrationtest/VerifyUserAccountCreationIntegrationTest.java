package uk.gov.ida.integrationtest;

import httpstub.HttpStubRule;
import io.dropwizard.testing.ConfigOverride;
import org.junit.ClassRule;
import org.junit.Test;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.core.Response;
import uk.gov.ida.integrationtest.helpers.AttributeFactory;
import uk.gov.ida.integrationtest.helpers.MatchingServiceAdapterAppRule;
import uk.gov.ida.saml.core.OpenSamlXmlObjectFactory;

import java.util.List;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.opensaml.saml.saml2.core.StatusCode.REQUESTER;
import static uk.gov.ida.integrationtest.helpers.AssertionHelper.aValidEidasAttributeQueryWithCycle3Attributes;
import static uk.gov.ida.integrationtest.helpers.RequestHelper.makeAttributeQueryRequest;
import static uk.gov.ida.matchingserviceadapter.domain.UserAccountCreationAttribute.*;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.TEST_RP_MS;
import static uk.gov.ida.saml.core.test.matchers.SignableSAMLObjectBaseMatcher.signedBy;

public class VerifyUserAccountCreationIntegrationTest extends UserAccountCreationBaseTest {
    private static final boolean IS_COUNTRY_ENABLED = false;

    @ClassRule
    public static final HttpStubRule localMatchingService = new HttpStubRule();
    @ClassRule
    public static final MatchingServiceAdapterAppRule applicationRule = new MatchingServiceAdapterAppRule(
        IS_COUNTRY_ENABLED,
        ConfigOverride.config("localMatchingService.matchUrl", "http://localhost:" + localMatchingService.getPort() + MATCHING_REQUEST_PATH),
        ConfigOverride.config("localMatchingService.accountCreationUrl", "http://localhost:" + localMatchingService.getPort() + UNKNOWN_USER_MATCHING_PATH)
    );

    @Override
    protected MatchingServiceAdapterAppRule getAppRule() {
        return applicationRule;
    }

    @Override
    protected HttpStubRule setUpMatchingService() throws Exception {
        localMatchingService.register(UNKNOWN_USER_MATCHING_PATH, 200, "application/json", "{\"result\": \"success\"}");
        return localMatchingService;
    }

    @Test
    public void shouldReturnFailureResponseWhenPassedEidasFullMatchingDataset() {
        List<Attribute> requiredAttributes = Stream.of(FIRST_NAME, FIRST_NAME_VERIFIED, MIDDLE_NAME, MIDDLE_NAME_VERIFIED, SURNAME, SURNAME_VERIFIED, CURRENT_ADDRESS, CURRENT_ADDRESS_VERIFIED, ADDRESS_HISTORY, CYCLE_3)
                .map(userAccountCreationAttribute -> new AttributeFactory(new OpenSamlXmlObjectFactory()).createAttribute(userAccountCreationAttribute))
                .collect(toList());

        AttributeQuery attributeQuery = aValidEidasAttributeQueryWithCycle3Attributes(REQUEST_ID, applicationRule.getCountryEntityId()).withAttributes(requiredAttributes).build();

        Response response = makeAttributeQueryRequest(UNKNOWN_USER_URI, attributeQuery, signatureAlgorithmForHub, digestAlgorithmForHub, HUB_ENTITY_ID);
        List<Assertion> decryptedAssertions = assertionDecrypter.decryptAssertions(response::getEncryptedAssertions);

        assertThat(response.getStatus().getStatusCode().getValue()).isEqualTo(REQUESTER);
        assertThat(decryptedAssertions).hasSize(0);
        assertThat(response.getInResponseTo()).isEqualTo(REQUEST_ID);
        assertThat(response.getIssuer().getValue()).isEqualTo(TEST_RP_MS);
        assertThat(response).is(signedBy(TEST_RP_MS_PUBLIC_SIGNING_CERT, TEST_RP_MS_PRIVATE_SIGNING_KEY));
    }
}
