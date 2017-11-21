package uk.gov.ida.matchingserviceadapter.services;

import org.opensaml.saml.saml2.core.Assertion;
import uk.gov.ida.matchingserviceadapter.domain.EidasLoa;
import uk.gov.ida.matchingserviceadapter.domain.EidasToVerifyLoaTransformer;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceRequestContext;
import uk.gov.ida.matchingserviceadapter.rest.MatchingServiceRequestDto;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.EidasMatchingDataset;

import com.google.common.base.Optional;
import uk.gov.ida.matchingserviceadapter.rest.matchingservice.LevelOfAssuranceDto;

import java.util.function.Function;

public class EidasMatchingRequestToLMSRequestTransform implements Function<MatchingServiceRequestContext, MatchingServiceRequestDto> {
    private EidasToVerifyLoaTransformer eidasToVerifyLoaTransformer = new EidasToVerifyLoaTransformer();

    @Override
    public MatchingServiceRequestDto apply(MatchingServiceRequestContext matchingServiceRequestContext) {
        Assertion assertion = matchingServiceRequestContext.getAssertions().get(0);

        return new MatchingServiceRequestDto((EidasMatchingDataset) null,
                                             Optional.absent(),
                                             null,
                                             null,
                                             extractVerifyLoa(assertion));
    }

    private LevelOfAssuranceDto extractVerifyLoa(Assertion assertion) {
        return eidasToVerifyLoaTransformer.apply(EidasLoa.valueOfUri(assertion.getAuthnStatements().get(0).getAuthnContext().getAuthnContextClassRef().getAuthnContextClassRef()));
    }
}
