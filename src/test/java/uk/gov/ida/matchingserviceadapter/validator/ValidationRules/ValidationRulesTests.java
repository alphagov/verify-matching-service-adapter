package uk.gov.ida.matchingserviceadapter.validator.ValidationRules;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.OneTimeUse;
import org.opensaml.saml.saml2.core.ProxyRestriction;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;
import uk.gov.ida.matchingserviceadapter.validators.ValidationRules.ConditionsElementMustNotBeNull;
import uk.gov.ida.matchingserviceadapter.validators.ValidationRules.ConditionsShouldNotContainOneTimeUseElement;
import uk.gov.ida.matchingserviceadapter.validators.ValidationRules.ConditionsShouldNotContainProxyRestrictionElement;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class ValidationRulesTests {
    Conditions conditions;

    @Rule
    public ExpectedException expectedException = ExpectedException.none();

    @Before
    public void setup() {
        conditions = mock(Conditions.class);
    }

    @Test
    public void shouldThrowExceptionWhenConditionsElementIsNull() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Conditions is missing from the assertion.");
        ConditionsElementMustNotBeNull.validate(null);
    }

    @Test
    public void ConditionsElementMustNotBeNullWillNotThrowExceptionWhenItIsNotNull() {
        ConditionsElementMustNotBeNull.validate(conditions);
    }

    @Test
    public void shouldThrowExceptionWhenConditionsContainOneTimeUseElement() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Conditions should not contain one time use element.");
        when(conditions.getOneTimeUse()).thenReturn(mock(OneTimeUse.class));

        ConditionsShouldNotContainOneTimeUseElement.validate(conditions.getOneTimeUse());
    }

    @Test
    public void shouldNotThrowExceptionWhenConditionsDoNotContainOneTimeUseElement() {
        ConditionsShouldNotContainOneTimeUseElement.validate(conditions.getOneTimeUse());
    }

    @Test
    public void shouldThrowExceptionWhenConditionsContainProxyRestrictionElement() {
        expectedException.expect(SamlResponseValidationException.class);
        expectedException.expectMessage("Conditions should not contain proxy restriction element.");
        when(conditions.getProxyRestriction()).thenReturn(mock(ProxyRestriction.class));

        ConditionsShouldNotContainProxyRestrictionElement.validate(conditions.getProxyRestriction());
    }

    @Test
    public void shouldNotThrowExceptionWhenConditionsDoNotContainProxyRestrictionElement() {
        ConditionsShouldNotContainProxyRestrictionElement.validate(conditions.getProxyRestriction());
    }
}
