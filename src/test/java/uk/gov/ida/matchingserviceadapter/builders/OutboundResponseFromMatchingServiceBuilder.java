package uk.gov.ida.matchingserviceadapter.builders;

import com.google.common.base.Optional;
import org.joda.time.DateTime;
import uk.gov.ida.matchingserviceadapter.domain.MatchingServiceAssertion;
import uk.gov.ida.matchingserviceadapter.domain.OutboundResponseFromMatchingService;
import uk.gov.ida.saml.core.domain.MatchingServiceIdaStatus;

import static com.google.common.base.Optional.absent;

public class OutboundResponseFromMatchingServiceBuilder extends ResponseBuilder<OutboundResponseFromMatchingServiceBuilder> {

    private Optional<MatchingServiceAssertion> matchingServiceAssertion = absent();

    public static OutboundResponseFromMatchingServiceBuilder aResponse() {
        return new OutboundResponseFromMatchingServiceBuilder()
                .withResponseId("response-id")
                .withInResponseTo("request-id")
                .withIssuerId("issuer-id")
                .withIssueInstant(DateTime.now())
                .withStatus(MatchingServiceIdaStatus.MatchingServiceMatch);
    }

    public OutboundResponseFromMatchingService build() {
        return new OutboundResponseFromMatchingService(
                responseId,
                inResponseTo,
                issuerId,
                issueInstant,
                status,
                matchingServiceAssertion);
    }
}
