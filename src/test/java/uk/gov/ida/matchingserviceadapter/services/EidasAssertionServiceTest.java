package uk.gov.ida.matchingserviceadapter.services;

import org.joda.time.LocalDate;
import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.mockito.Mock;
import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.matchingserviceadapter.domain.AssertionData;
import uk.gov.ida.matchingserviceadapter.validators.CountryConditionsValidator;
import uk.gov.ida.matchingserviceadapter.validators.InstantValidator;
import uk.gov.ida.matchingserviceadapter.validators.SubjectValidator;
import uk.gov.ida.saml.core.IdaSamlBootstrap;
import uk.gov.ida.saml.core.transformers.EidasMatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.transformers.inbound.Cycle3DatasetFactory;
import uk.gov.ida.saml.metadata.MetadataResolverRepository;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.validators.ValidatedAssertions;
import uk.gov.ida.shared.utils.datetime.DateTimeFreezer;

import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.initMocks;
import static uk.gov.ida.saml.core.domain.AuthnContext.LEVEL_2;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_CONNECTOR_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_COUNTRY_ONE;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.aCycle3DatasetAssertion;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anEidasAssertion;

public class EidasAssertionServiceTest {

    private EidasAssertionService eidasAssertionService;

    @Mock
    private InstantValidator instantValidator;

    @Mock
    private SubjectValidator subjectValidator;

    @Mock
    private CountryConditionsValidator conditionsValidator;

    @Mock
    private SamlAssertionsSignatureValidator hubSignatureValidator;

    @Mock
    private MetadataResolverRepository metadataResolverRepository;

    @Before
    public void setUp() {
        IdaSamlBootstrap.bootstrap();
        initMocks(this);
        eidasAssertionService = new EidasAssertionService(
                instantValidator,
                subjectValidator,
                conditionsValidator,
                hubSignatureValidator,
                new Cycle3DatasetFactory(),
                metadataResolverRepository,
                new String[] { HUB_CONNECTOR_ENTITY_ID },
                HUB_ENTITY_ID,
                new EidasMatchingDatasetUnmarshaller()
        );
        doNothing().when(instantValidator).validate(any(), any());
        doNothing().when(subjectValidator).validate(any(), any());
        doNothing().when(conditionsValidator).validate(any(), any());
        when(hubSignatureValidator.validate(any(), any())).thenReturn(mock(ValidatedAssertions.class));
        when(metadataResolverRepository.getResolverEntityIds()).thenReturn(Arrays.asList(STUB_COUNTRY_ONE));

        DateTimeFreezer.freezeTime();
    }

    @After
    public void tearDown() {
        DateTimeFreezer.unfreezeTime();
    }

    //This Test would need to mock out a hierarchy of calls in the MetadataResolverRepository
    //and would just be testing a bunch of wiring.
    @Ignore
    @Test
    public void shouldCallValidatorsCorrectly() {

        List<Assertion> assertions = asList(anEidasAssertion().buildUnencrypted());

        eidasAssertionService.validate("requestId", assertions);
        verify(instantValidator, times(1)).validate(any(), any());
        verify(subjectValidator, times(1)).validate(any(), any());
        verify(conditionsValidator, times(1)).validate(any(), any());
        verify(hubSignatureValidator, times(1)).validate(any(), any());
    }

    @Test
    public void shouldTranslateEidasAssertion() {
        Assertion eidasAssertion = anEidasAssertion().buildUnencrypted();
        Assertion cycle3Assertion = aCycle3DatasetAssertion("NI", "123456").buildUnencrypted();
        List<Assertion> assertions = asList( eidasAssertion, cycle3Assertion);
        AssertionData assertionData = eidasAssertionService.translate(assertions);

        assertThat(assertionData.getLevelOfAssurance()).isEqualTo(LEVEL_2);
        assertThat(assertionData.getMatchingDatasetIssuer()).isEqualTo(STUB_COUNTRY_ONE);
        assertThat(assertionData.getCycle3Data().get().getAttributes().get("NI")).isEqualTo("123456");
        assertThat(assertionData.getMatchingDataset().getFirstNames().get(0).getValue()).isEqualTo("Joe");
        assertThat(assertionData.getMatchingDataset().getSurnames().get(0).getValue()).isEqualTo("Bloggs");
        assertThat(assertionData.getMatchingDataset().getPersonalId()).isEqualTo("JB12345");
        assertThat(assertionData.getMatchingDataset().getDateOfBirths().get(0).getValue()).isEqualTo(LocalDate.now());
    }
}
