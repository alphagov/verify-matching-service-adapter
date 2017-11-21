package uk.gov.ida.matchingserviceadapter.services;

import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
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
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.aBirthNameAttribute;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.aCurrentFamilyNameAttribute;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.aCurrentGivenNameAttribute;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.aDateOfBirthAttribute;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.aGenderAttribute;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.aPersonIdentifierAttribute;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.aPlaceOfBirthAttribute;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.anEidasAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static uk.gov.ida.saml.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;

@RunWith(OpenSAMLMockitoRunner.class)
public class EidasMatchingRequestToLMSRequestTransformTest {
    public static final org.joda.time.LocalDate DOB = org.joda.time.LocalDate.parse("2001-02-01", ISODateTimeFormat.dateTimeParser());
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
            )
            .addAttributeStatement(
                anEidasAttributeStatement(
                    aCurrentGivenNameAttribute("Fred"),
                    aCurrentFamilyNameAttribute("Flintstone"),
                    aPersonIdentifierAttribute("the-pid"),
                    aGenderAttribute("MALE"),
                    aDateOfBirthAttribute(DOB),
                    aBirthNameAttribute("birth-name"),
                    aPlaceOfBirthAttribute("place-of-birth")
                ).build()
            ).buildUnencrypted();
        transform = new EidasMatchingRequestToLMSRequestTransform();
    }

    @Test
    public void shouldMapLoaCorrectly() {
        MatchingServiceRequestContext request = new MatchingServiceRequestContext(null, null, asList(assertion));

        MatchingServiceRequestDto lmsDto = transform.apply(request);

        assertThat(lmsDto.getLevelOfAssurance(), notNullValue());
        assertThat(lmsDto.getLevelOfAssurance().name(), equalTo(LevelOfAssuranceDto.LEVEL_2.name()));

    }

    @Test
    public void shouldExtractPidCorrectly() throws Throwable {
        MatchingServiceRequestContext request = new MatchingServiceRequestContext(null, null, asList(assertion));

        MatchingServiceRequestDto lmsDto = transform.apply(request);

        assertThat(lmsDto.getHashedPid(), equalTo("the-pid"));
    }

    @Test
    public void shouldMapEidasMatchingDatasetCorrectly() {
        MatchingServiceRequestContext request = new MatchingServiceRequestContext(null, null, asList(assertion));

        MatchingServiceRequestDto lmsDto = transform.apply(request);

        assertThat(lmsDto.getEidasDataset(), notNullValue());
        assertThat(lmsDto.getEidasDataset().getFirstName(), equalTo("Fred"));
        assertThat(lmsDto.getEidasDataset().getDateOfBirth(), equalTo(DOB));
        assertThat(lmsDto.getEidasDataset().getFamilyName(), equalTo("Flintstone"));
        assertThat(lmsDto.getEidasDataset().getGender(), equalTo("MALE"));
        assertThat(lmsDto.getEidasDataset().getBirthName(), equalTo("birth-name"));
        assertThat(lmsDto.getEidasDataset().getPlaceOfBirth(), equalTo("place-of-birth"));

    }
}