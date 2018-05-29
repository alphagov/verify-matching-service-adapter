package uk.gov.ida.matchingserviceadapter.domain;

import org.joda.time.DateTime;
import uk.gov.ida.common.shared.security.IdGenerator;
import uk.gov.ida.saml.core.domain.IdaMatchingServiceResponse;
import uk.gov.ida.saml.core.domain.UnknownUserCreationIdaStatus;

import java.util.Optional;

public class OutboundResponseFromUnknownUserCreationService extends IdaMatchingServiceResponse {
    private final UnknownUserCreationIdaStatus status;
    private final Optional<MatchingServiceAssertion> matchingServiceAssertion;

    public OutboundResponseFromUnknownUserCreationService(
            String responseId,
            String inResponseTo,
            String issuer,
            DateTime issueInstant,
            UnknownUserCreationIdaStatus status,
            Optional<MatchingServiceAssertion> matchingServiceAssertion) {
        super(responseId, inResponseTo, issuer, issueInstant);
        this.status = status;
        this.matchingServiceAssertion = matchingServiceAssertion;
    }

    public static OutboundResponseFromUnknownUserCreationService createFailure(
            String responseId,
            String originalRequestId,
            String issuerId) {
        return new OutboundResponseFromUnknownUserCreationService(
                responseId,
                originalRequestId,
                issuerId,
                DateTime.now(),
                UnknownUserCreationIdaStatus.CreateFailure,
                Optional.empty());
    }

    public static OutboundResponseFromUnknownUserCreationService createNoAttributeFailure(
            String responseId,
            String originalRequestId,
            String issuerId) {
        return new OutboundResponseFromUnknownUserCreationService(
                responseId,
                originalRequestId,
                issuerId,
                DateTime.now(),
                UnknownUserCreationIdaStatus.NoAttributeFailure,
                Optional.empty());
    }

    public static OutboundResponseFromUnknownUserCreationService createSuccess(
            String responseId,
            MatchingServiceAssertion assertion,
            String originalRequestId,
            String issuerId) {
        return new OutboundResponseFromUnknownUserCreationService(
                responseId,
                originalRequestId,
                issuerId,
                DateTime.now(),
                UnknownUserCreationIdaStatus.Success,
                Optional.ofNullable(assertion)
        );
    }

    public Optional<MatchingServiceAssertion> getMatchingServiceAssertion() {
        return matchingServiceAssertion;
    }

    public UnknownUserCreationIdaStatus getStatus() {
        return status;
    }
}
