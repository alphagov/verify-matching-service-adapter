package uk.gov.ida.matchingserviceadapter.services;

import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AuthnStatement;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import uk.gov.ida.matchingserviceadapter.domain.AssertionData;
import uk.gov.ida.matchingserviceadapter.exceptions.SamlResponseValidationException;
import uk.gov.ida.matchingserviceadapter.validators.ConditionsValidator;
import uk.gov.ida.matchingserviceadapter.validators.InstantValidator;
import uk.gov.ida.matchingserviceadapter.validators.SubjectValidator;
import uk.gov.ida.saml.core.transformers.AuthnContextFactory;
import uk.gov.ida.saml.core.transformers.EidasMatchingDatasetUnmarshaller;
import uk.gov.ida.saml.core.transformers.inbound.Cycle3DatasetFactory;
import uk.gov.ida.saml.metadata.MetadataResolverRepository;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SamlMessageSignatureValidator;

import javax.inject.Inject;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;

public class EidasAssertionService extends AssertionService {

    private final ConditionsValidator conditionsValidator;
    private final MetadataResolverRepository metadataResolverRepository;
    private final String hubConnectorEntityId;
    private final String hubEntityId;
    private final EidasMatchingDatasetUnmarshaller matchingDatasetUnmarshaller;
    private final AuthnContextFactory authnContextFactory = new AuthnContextFactory();

    @Inject
    public EidasAssertionService(InstantValidator instantValidator,
                                 SubjectValidator subjectValidator,
                                 ConditionsValidator conditionsValidator,
                                 SamlAssertionsSignatureValidator hubSignatureValidator,
                                 Cycle3DatasetFactory cycle3DatasetFactory,
                                 MetadataResolverRepository metadataResolverRepository,
                                 String hubConnectorEntityId,
                                 String hubEntityId,
                                 EidasMatchingDatasetUnmarshaller matchingDatasetUnmarshaller) {
        super(instantValidator, subjectValidator, conditionsValidator, hubSignatureValidator, cycle3DatasetFactory);
        this.conditionsValidator = conditionsValidator;
        this.metadataResolverRepository = metadataResolverRepository;
        this.hubConnectorEntityId = hubConnectorEntityId;
        this.hubEntityId = hubEntityId;
        this.matchingDatasetUnmarshaller = matchingDatasetUnmarshaller;
    }

    @Override
    void validate(String expectedInResponseTo, List<Assertion> assertions) {
        for (Assertion assertion : assertions){
            if(isCountryAssertion(assertion)){
                validateCountryAssertion(assertion, expectedInResponseTo);
            }else if(isHubAssertion(assertion)){
                validateCycle3Assertion(assertion, expectedInResponseTo, hubEntityId);
            }else{
                throw new SamlResponseValidationException("Unknown Issuer for eIDAS Assertion: "+assertion.getIssuer().getValue());
            }
        }
    }

    @Override
    public AssertionData translate(List<Assertion> assertions) {
        Assertion countryAssertion = assertions
                .stream()
                .filter(this::isCountryAssertion)
                .findFirst()
                .orElseThrow(() -> new SamlResponseValidationException("No matching dataset assertion present."));
        Optional<Assertion> cycle3Assertion = assertions.stream()
                .filter(a -> !isCountryAssertion(a))
                .findFirst();

        AuthnStatement authnStatement = countryAssertion.getAuthnStatements().get(0);
        String levelOfAssurance = authnStatement.getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef();
        return new AssertionData(countryAssertion.getIssuer().getValue(),
                authnContextFactory.mapFromEidasToLoA(levelOfAssurance),
                getCycle3Data(cycle3Assertion),
                matchingDatasetUnmarshaller.fromAssertion(countryAssertion));
    }

    private void validateCountryAssertion(Assertion assertion, String expectedInResponseTo) {
        metadataResolverRepository.getSignatureTrustEngine(assertion.getIssuer().getValue())
                .map(MetadataBackedSignatureValidator::withoutCertificateChainValidation)
                .map(SamlMessageSignatureValidator::new)
                .map(SamlAssertionsSignatureValidator::new)
                .orElseThrow(() -> new SamlResponseValidationException("Unable to find metadata resolver for entity Id " + assertion.getIssuer().getValue()))
                .validate(asList(assertion), IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        instantValidator.validate(assertion.getIssueInstant(), "Country Assertion IssueInstant");
        subjectValidator.validate(assertion.getSubject(), expectedInResponseTo);
        conditionsValidator.validate(assertion.getConditions(), hubConnectorEntityId);
    }

    public Boolean isCountryAssertion(Assertion assertion) {
        return metadataResolverRepository.getResolverEntityIds().contains(assertion.getIssuer().getValue());
    }

    public Boolean isHubAssertion(Assertion assertion) {
        return assertion.getIssuer().getValue().equals(hubEntityId);
    }

}
