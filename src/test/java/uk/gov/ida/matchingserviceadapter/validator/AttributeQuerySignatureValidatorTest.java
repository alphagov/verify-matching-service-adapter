package uk.gov.ida.matchingserviceadapter.validator;

import org.junit.Before;
import org.junit.Test;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import uk.gov.ida.matchingserviceadapter.validators.AttributeQuerySignatureValidator;
import uk.gov.ida.saml.core.validation.SamlTransformationErrorException;
import uk.gov.ida.saml.core.validation.SamlValidationResponse;
import uk.gov.ida.saml.core.validation.SamlValidationSpecificationFailure;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class AttributeQuerySignatureValidatorTest {

    private AttributeQuerySignatureValidator validator;
    private SamlMessageSignatureValidator samlMessageSignatureValidator;

    @Before
    public void setUp() {

        samlMessageSignatureValidator = mock(SamlMessageSignatureValidator.class);
    }

    @Test
    public void shouldNotComplainWhenCorrectDataIsPassed() {
        AttributeQuery attributeQuery = mock(AttributeQuery.class);
        SamlValidationResponse samlValidationResponse = SamlValidationResponse.aValidResponse();
        when(samlMessageSignatureValidator.validate(attributeQuery, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(samlValidationResponse);
        validator = new AttributeQuerySignatureValidator(samlMessageSignatureValidator);

        validator.validate(attributeQuery);
    }

    @Test(expected = SamlTransformationErrorException.class)
    public void shouldThrowExceptionWhenInvalidDataPassed(){
        AttributeQuery attributeQuery = mock(AttributeQuery.class);
        SamlValidationSpecificationFailure samlValidationSpecificationFailure = mock(SamlValidationSpecificationFailure.class);
        SamlValidationResponse samlValidationResponse = SamlValidationResponse.anInvalidResponse(samlValidationSpecificationFailure);
        when(samlMessageSignatureValidator.validate(attributeQuery, SPSSODescriptor.DEFAULT_ELEMENT_NAME)).thenReturn(samlValidationResponse);

        validator = new AttributeQuerySignatureValidator(samlMessageSignatureValidator);

        validator.validate(attributeQuery);

    }

}
