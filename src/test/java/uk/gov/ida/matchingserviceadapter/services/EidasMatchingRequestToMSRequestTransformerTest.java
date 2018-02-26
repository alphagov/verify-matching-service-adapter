package uk.gov.ida.matchingserviceadapter.services;

import com.google.common.base.Optional;
import org.joda.time.LocalDate;
import org.joda.time.format.ISODateTimeFormat;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.rest.VerifyMatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;
import uk.gov.ida.matchingserviceadapter.saml.UserIdHashFactory;
import uk.gov.ida.saml.core.test.OpenSAMLMockitoRunner;
import uk.gov.ida.saml.core.transformers.inbound.Cycle3DatasetFactory;
import uk.gov.ida.saml.core.transformers.inbound.HubAssertionUnmarshaller;
import uk.gov.ida.saml.hub.domain.LevelOfAssurance;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.notNullValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.aBirthNameAttribute;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.aCurrentFamilyNameAttribute;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.aCurrentGivenNameAttribute;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.aDateOfBirthAttribute;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.aGenderAttribute;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.aPersonIdentifierAttribute;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.aPlaceOfBirthAttribute;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.matchingserviceadapter.services.AttributeStatementBuilder.anEidasAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static uk.gov.ida.saml.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.SimpleStringAttributeBuilder.aSimpleStringAttribute;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.*;

@RunWith(OpenSAMLMockitoRunner.class)
public class EidasMatchingRequestToMSRequestTransformerTest {
    private static final LocalDate DOB = LocalDate.parse("2001-02-01", ISODateTimeFormat.dateTimeParser());
    private static final LevelOfAssurance levelOfAssurance = LevelOfAssurance.SUBSTANTIAL;
    private static final String personIdentifier = "the-pid";
    private static final String issuerId = "issuer-id";
    private static final String hubEntityId = "hub-id";

    private EidasMatchingRequestToMSRequestTransformer transform;

    @Mock
    private AttributeQuery attributeQuery;

    @Mock
    private UserIdHashFactory pidHashFactory;

    @Before
    public void setUp() {
        when(attributeQuery.getID()).thenReturn("the-aqr-id");
        when(pidHashFactory.hashId(issuerId, personIdentifier, Optional.of(levelOfAssurance.toVerifyLevelOfAssurance()))).thenReturn("the-hashed-pid");
        transform = new EidasMatchingRequestToMSRequestTransformer(pidHashFactory, hubEntityId, new HubAssertionUnmarshaller(new Cycle3DatasetFactory(), hubEntityId));
    }

    private Assertion makeAssertion() {
        return anAssertion()
            .withIssuer(
                anIssuer()
                    .withIssuerId(issuerId)
                .build()
            )
            .addAuthnStatement(
                anAuthnStatement()
                    .withAuthnContext(anAuthnContext()
                        .withAuthnContextClassRef(
                            anAuthnContextClassRef()
                            .withAuthnContextClasRefValue(levelOfAssurance.toString())
                            .build()
                        ).build()
                    )
                .build()
            )
            .addAttributeStatement(
                anEidasAttributeStatement(
                    aCurrentGivenNameAttribute("Fred"),
                    aCurrentFamilyNameAttribute("Flintstone"),
                    aPersonIdentifierAttribute(personIdentifier),
                    aGenderAttribute("MALE"),
                    aDateOfBirthAttribute(DOB),
                    aBirthNameAttribute("birth-name"),
                    aPlaceOfBirthAttribute("place-of-birth")
                ).build()
            ).buildUnencrypted();
    }

    private Assertion makeCycle3Assertion() {
        return anAssertion()
            .withIssuer(
                anIssuer()
                    .withIssuerId(hubEntityId)
                .build()
            )
            .addAttributeStatement(
                anAttributeStatement(
                    aSimpleStringAttribute()
                        .withName("NI")
                        .withSimpleStringValue("12345")
                    .build()
                ).build()
            ).buildUnencrypted();
    }

    @Test
    public void shouldMapLoaCorrectly() {
        MatchingServiceRequestContext request = new MatchingServiceRequestContext(null, attributeQuery, asList(makeAssertion()));

        VerifyMatchingServiceRequestDto lmsDto = transform.apply(request);

        assertThat(lmsDto.getLevelOfAssurance(), notNullValue());
        assertThat(lmsDto.getLevelOfAssurance().name(), equalTo(LevelOfAssuranceDto.LEVEL_2.name()));

    }

    @Test
    public void shouldExtractPidCorrectly() {
        MatchingServiceRequestContext request = new MatchingServiceRequestContext(null, attributeQuery, asList(makeAssertion()));

        VerifyMatchingServiceRequestDto lmsDto = transform.apply(request);

        assertThat(lmsDto.getHashedPid(), equalTo("the-hashed-pid"));
    }

    @Test
    public void shouldMapEidasMatchingDatasetCorrectly() {
        MatchingServiceRequestContext request = new MatchingServiceRequestContext(null, attributeQuery, asList(makeAssertion()));

        VerifyMatchingServiceRequestDto lmsDto = transform.apply(request);

        assertThat(lmsDto.getEidasDataset(), notNullValue());
        assertThat(lmsDto.getEidasDataset().getFirstName(), equalTo("Fred"));
        assertThat(lmsDto.getEidasDataset().getDateOfBirth(), equalTo(DOB));
        assertThat(lmsDto.getEidasDataset().getFamilyName(), equalTo("Flintstone"));
        assertThat(lmsDto.getEidasDataset().getGender(), equalTo("MALE"));
        assertThat(lmsDto.getEidasDataset().getBirthName(), equalTo("birth-name"));
        assertThat(lmsDto.getEidasDataset().getPlaceOfBirth(), equalTo("place-of-birth"));

    }

    @Test
    public void shouldExtractCycle3DataCorrectly() {
        MatchingServiceRequestContext request = new MatchingServiceRequestContext(null, attributeQuery, asList(makeAssertion(), makeCycle3Assertion()));

        VerifyMatchingServiceRequestDto lmsDto = transform.apply(request);

        assertThat(lmsDto.getCycle3Dataset().orNull(), notNullValue());
        assertThat(lmsDto.getCycle3Dataset().get().getAttributes().size(), equalTo(1));
        assertThat(lmsDto.getCycle3Dataset().get().getAttributes().get("NI"), equalTo("12345"));
    }
}
