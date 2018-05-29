package uk.gov.ida.matchingserviceadapter.domain;

import org.joda.time.DateTime;
import uk.gov.ida.common.shared.security.IdGenerator;
import uk.gov.ida.saml.core.domain.IdaMatchingServiceResponse;
import uk.gov.ida.saml.core.domain.MatchingServiceIdaStatus;

import java.util.Optional;

public class OutboundResponseFromMatchingService extends IdaMatchingServiceResponse {

    private Optional<MatchingServiceAssertion> matchingServiceAssertion;
    private MatchingServiceIdaStatus status;

    public OutboundResponseFromMatchingService(
            String responseId,
            String inResponseTo,
            String issuer,
            DateTime issueInstant,
            MatchingServiceIdaStatus status,
            Optional<MatchingServiceAssertion> matchingServiceAssertion) {

        super(responseId, inResponseTo, issuer, issueInstant);

        this.matchingServiceAssertion = matchingServiceAssertion;
        this.status = status;
    }

    public Optional<MatchingServiceAssertion> getMatchingServiceAssertion() {
        return matchingServiceAssertion;
    }

    public static OutboundResponseFromMatchingService createMatchFromMatchingService(
            String responseId,
            MatchingServiceAssertion assertion,
            String originalRequestId,
            String issuerId) {
        return new OutboundResponseFromMatchingService(
                responseId,
                originalRequestId,
                issuerId,
                DateTime.now(),
                MatchingServiceIdaStatus.MatchingServiceMatch,
                Optional.ofNullable(assertion)
        );
    }

    public static OutboundResponseFromMatchingService createNoMatchFromMatchingService(
            String responseId,
            String originalRequestId,
            String issuerId) {
        return new OutboundResponseFromMatchingService(
                responseId,
                originalRequestId,
                issuerId,
                DateTime.now(),
                MatchingServiceIdaStatus.NoMatchingServiceMatchFromMatchingService,
                Optional.empty()
        );
    }

    public MatchingServiceIdaStatus getStatus() {
        return status;
    }
}
