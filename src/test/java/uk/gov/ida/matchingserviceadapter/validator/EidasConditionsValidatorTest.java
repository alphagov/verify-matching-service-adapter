package uk.gov.ida.matchingserviceadapter.validator;

import com.google.common.collect.ImmutableList;
import org.joda.time.DateTime;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.OneTimeUse;
import org.opensaml.saml.saml2.core.ProxyRestriction;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;
import uk.gov.ida.matchingserviceadapter.validators.AssertionTimeRestrictionValidator;
import uk.gov.ida.matchingserviceadapter.validators.AudienceRestrictionValidator;
import uk.gov.ida.matchingserviceadapter.validators.EidasConditionsValidator;
import uk.gov.ida.saml.core.IdaSamlBootstrap;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static uk.gov.ida.saml.core.test.builders.AudienceRestrictionBuilder.anAudienceRestriction;

public class EidasConditionsValidatorTest {

    private AssertionTimeRestrictionValidator timeRestrictionValidator;
    private AudienceRestrictionValidator audienceRestrictionValidator;
    private Conditions conditions;

    private EidasConditionsValidator validator;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setUp() {
        timeRestrictionValidator = mock(AssertionTimeRestrictionValidator.class);
        audienceRestrictionValidator = mock(AudienceRestrictionValidator.class);
        conditions = mock(Conditions.class);

        validator = new EidasConditionsValidator(timeRestrictionValidator, audienceRestrictionValidator);

        IdaSamlBootstrap.bootstrap();
    }

    @Test
    public void shouldThrowExceptionWhenConditionsIsNull() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Conditions is missing from the assertion.");

        validator.validate(null, "any-entity-id");
    }

    @Test
    public void shouldThrowExceptionWhenProxyRestrictionElementExists() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Conditions should not contain proxy restriction element.");

        when(conditions.getProxyRestriction()).thenReturn(mock(ProxyRestriction.class));

        validator.validate(conditions, "any-entity-id");
    }

    @Test
    public void shouldNotThrowExceptionWhenOneTimeUseElementExists() {
        when(conditions.getOneTimeUse()).thenReturn(mock(OneTimeUse.class));
        validator.validate(conditions, "any-entity-id");
    }

    @Test
    public void shouldValidateNotOnOrAfterIfExists() {
        DateTime notOnOrAfter = new DateTime();
        when(conditions.getNotOnOrAfter()).thenReturn(notOnOrAfter);

        validator.validate(conditions, "any-entity-id");

        verify(timeRestrictionValidator).validateNotOnOrAfter(notOnOrAfter);
    }

    @Test
    public void shouldNotValidateNotOnOrAfterIfExists() {
        DateTime notOnOrAfter = null;
        when(conditions.getNotOnOrAfter()).thenReturn(notOnOrAfter);

        validator.validate(conditions, "any-entity-id");
    }

    @Test
    public void shouldValidateConditionsNotBefore() {
        DateTime notBefore = new DateTime();
        when(conditions.getNotBefore()).thenReturn(notBefore);

        validator.validate(conditions, "any-entity-id");

        verify(timeRestrictionValidator).validateNotBefore(notBefore);
    }

    @Test
    public void shouldValidateConditionsAudienceRestrictions() {
        List<AudienceRestriction> audienceRestrictions = ImmutableList.of(anAudienceRestriction().build());
        when(conditions.getAudienceRestrictions()).thenReturn(audienceRestrictions);

        validator.validate(conditions, "some-entity-id");

        verify(audienceRestrictionValidator).validate(audienceRestrictions, "some-entity-id");
    }
}