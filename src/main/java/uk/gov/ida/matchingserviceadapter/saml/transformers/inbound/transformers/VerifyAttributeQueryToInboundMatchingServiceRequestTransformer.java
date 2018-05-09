package uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.transformers;

import com.google.inject.Inject;
import net.shibboleth.utilities.java.support.resolver.Criterion;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.AttributeQuery;
import org.opensaml.saml.saml2.metadata.IDPSSODescriptor;
import org.opensaml.saml.saml2.metadata.SPSSODescriptor;
import org.opensaml.security.SecurityException;
import org.opensaml.security.trust.TrustEngine;
import org.opensaml.xmlsec.signature.Signature;
import org.opensaml.xmlsec.signature.support.SignatureException;
import uk.gov.ida.matchingserviceadapter.saml.security.AttributeQuerySignatureValidator;
import uk.gov.ida.matchingserviceadapter.saml.security.ValidatedAttributeQuery;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.InboundVerifyMatchingServiceRequest;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.decorators.SamlAttributeQueryAssertionsValidator;
import uk.gov.ida.matchingserviceadapter.saml.transformers.inbound.decorators.SamlAttributeQueryValidator;
import uk.gov.ida.saml.metadata.MetadataResolverRepository;
import uk.gov.ida.saml.security.AssertionDecrypter;
import uk.gov.ida.saml.security.MetadataBackedSignatureValidator;
import uk.gov.ida.saml.security.SamlAssertionsSignatureValidator;
import uk.gov.ida.saml.security.SignatureValidator;
import uk.gov.ida.saml.security.validators.ValidatedAssertions;

import javax.inject.Named;
import javax.xml.namespace.QName;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class VerifyAttributeQueryToInboundMatchingServiceRequestTransformer implements Function<AttributeQuery, InboundVerifyMatchingServiceRequest> {

    public static final String HUB = "HUB";
    public static final String COUNTRY = "COUNTRY";
    public static final String IDP = "IDP";
    private final SamlAttributeQueryValidator samlAttributeQueryValidator;
    private final AttributeQuerySignatureValidator attributeQuerySignatureValidator;
    private final SamlAssertionsSignatureValidator samlAssertionsSignatureValidator;

    private final InboundMatchingServiceRequestUnmarshaller inboundMatchingServiceRequestUnmarshaller;

    private final SamlAttributeQueryAssertionsValidator samlAttributeQueryAssertionsValidator;
    private final MetadataResolverRepository eidasMetadataResolverRepository;
    private final AssertionDecrypter assertionDecrypter;
    private final String hubEntityId;

    @Inject
    public VerifyAttributeQueryToInboundMatchingServiceRequestTransformer(
            final SamlAttributeQueryValidator samlAttributeQueryValidator,
            final AttributeQuerySignatureValidator attributeQuerySignatureValidator,
            final SamlAssertionsSignatureValidator samlAssertionsSignatureValidator,
            final InboundMatchingServiceRequestUnmarshaller inboundMatchingServiceRequestUnmarshaller,
            final SamlAttributeQueryAssertionsValidator samlAttributeQueryAssertionsValidator,
            final MetadataResolverRepository eidasMetadataResolverRepository,
            final AssertionDecrypter assertionDecrypter,
            @Named("HubEntityId") final String hubEntityId) {

        this.samlAttributeQueryValidator = samlAttributeQueryValidator;
        this.attributeQuerySignatureValidator = attributeQuerySignatureValidator;
        this.samlAssertionsSignatureValidator = samlAssertionsSignatureValidator;
        this.inboundMatchingServiceRequestUnmarshaller = inboundMatchingServiceRequestUnmarshaller;
        this.samlAttributeQueryAssertionsValidator = samlAttributeQueryAssertionsValidator;
        this.eidasMetadataResolverRepository = eidasMetadataResolverRepository;
        this.assertionDecrypter = assertionDecrypter;
        this.hubEntityId = hubEntityId;
    }

    public InboundVerifyMatchingServiceRequest apply(final AttributeQuery attributeQuery) {
        samlAttributeQueryValidator.validate(attributeQuery);
        ValidatedAttributeQuery validatedAttributeQuery = attributeQuerySignatureValidator.validate(attributeQuery);

        List<Assertion> assertions = assertionDecrypter.decryptAssertions(validatedAttributeQuery);

        Map<String, List<Assertion>> map = assertions.stream().collect(Collectors.groupingBy(this::classifyAssertion));
        List<Assertion> hubAssertions = map.getOrDefault(HUB, Collections.emptyList());
        List<Assertion> idpAssertions = map.getOrDefault(IDP, Collections.emptyList());
        List<Assertion> countryAssertions = map.getOrDefault(COUNTRY, Collections.emptyList());

        samlAttributeQueryAssertionsValidator.validateHubAssertions(validatedAttributeQuery, hubAssertions);
        samlAttributeQueryAssertionsValidator.validateIdpAssertions(validatedAttributeQuery, idpAssertions);
        samlAttributeQueryAssertionsValidator.validateCountryAssertions(validatedAttributeQuery, countryAssertions);

        ValidatedAssertions validatedHubAssertions = samlAssertionsSignatureValidator.validate(hubAssertions, SPSSODescriptor.DEFAULT_ELEMENT_NAME);
        ValidatedAssertions validatedIdpAssertions = samlAssertionsSignatureValidator.validate(idpAssertions, IDPSSODescriptor.DEFAULT_ELEMENT_NAME);
        ValidatedAssertions validatedCountryAssertions = new ValidatedAssertions(
                countryAssertions.stream().map(assertion -> {
                    try {
                        MetadataBackedSignatureValidator.withoutCertificateChainValidation(eidasMetadataResolverRepository.getSignatureTrustEngine(assertion.getIssuer().getValue()).get());
                        if (MetadataBackedSignatureValidator.withoutCertificateChainValidation(eidasMetadataResolverRepository.getSignatureTrustEngine(assertion.getIssuer().getValue()).get())
                                .validate(assertion, assertion.getIssuer().getValue(), IDPSSODescriptor.DEFAULT_ELEMENT_NAME)) {
                            return assertion;
                        } else {
                            return null;
                        }
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                }
            ).collect(Collectors.toList())
        );
        return this.inboundMatchingServiceRequestUnmarshaller.fromSaml(validatedAttributeQuery, validatedHubAssertions, validatedIdpAssertions, validatedCountryAssertions);
    }

    private boolean isHubAssertion(Assertion assertion) {
        return assertion.getIssuer().getValue().equals(hubEntityId);
    }

    private boolean isCountryAssertion(Assertion assertion) {
        return eidasMetadataResolverRepository.getMetadataResolver(assertion.getIssuer().getValue()).isPresent();
    }

    private String classifyAssertion(Assertion assertion) {
        if (isHubAssertion(assertion)) {
            return HUB;
        } else if (isCountryAssertion(assertion)) {
            return COUNTRY;
        }
        return IDP;
    }

}
