package uk.gov.ida.integrationtest.helpers;

import org.joda.time.DateTime;
import org.opensaml.saml.saml2.core.Assertion;
import org.opensaml.saml.saml2.core.Attribute;
import org.opensaml.saml.saml2.core.Audience;
import org.opensaml.saml.saml2.core.AudienceRestriction;
import org.opensaml.saml.saml2.core.Conditions;
import org.opensaml.saml.saml2.core.EncryptedAssertion;
import org.opensaml.saml.saml2.core.NameID;
import org.opensaml.saml.saml2.core.Subject;
import org.opensaml.saml.saml2.core.SubjectConfirmation;
import org.opensaml.saml.saml2.core.SubjectConfirmationData;
import org.opensaml.saml.saml2.core.impl.AudienceBuilder;
import org.opensaml.saml.saml2.core.impl.AudienceRestrictionBuilder;
import org.opensaml.saml.saml2.core.impl.ConditionsBuilder;
import org.opensaml.xmlsec.signature.Signature;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder;
import uk.gov.ida.saml.core.test.builders.SubjectBuilder;
import uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder;
import uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder;

import java.util.List;

import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_SECONDARY_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_IDP_ONE;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.IPAddressAttributeBuilder.anIPAddress;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.NameIdBuilder.aNameId;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

public class AssertionHelper {

    private static Signature aValidSignature() {
        return aSignature()
                .withSigningCredential(
                        new TestCredentialFactory(
                                STUB_IDP_PUBLIC_PRIMARY_CERT,
                                STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY
                        ).getSigningCredential()
                ).build();
    }

    public static Assertion anAuthnStatementAssertion() {
        return anAssertion()
                .addAuthnStatement(anAuthnStatement().build())
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                .addAttributeStatement(anAttributeStatement().addAttribute(anIPAddress().build()).build())
                .buildUnencrypted();
    }

    public static Subject aSubjectWithAssertions(List<Assertion> assertions, String requestId, String hubEntityId) {
        final NameID nameId = aNameId().withNameQualifier("").withSpNameQualifier(hubEntityId).build();
        SubjectConfirmationDataBuilder subjectConfirmationDataBuilder = aSubjectConfirmationData().withInResponseTo(requestId);
        assertions.stream().forEach(subjectConfirmationDataBuilder::addAssertion);
        final SubjectConfirmationData subjectConfirmationData = subjectConfirmationDataBuilder.build();
        final SubjectConfirmation subjectConfirmation = SubjectConfirmationBuilder.aSubjectConfirmation()
                .withSubjectConfirmationData(subjectConfirmationData).build();

        return SubjectBuilder.aSubject().withNameId(nameId).withSubjectConfirmation(subjectConfirmation).build();
    }

    public static Subject aSubjectWithEncryptedAssertions(List<EncryptedAssertion> assertions, String requestId, String hubEntityId) {
        final NameID nameId = aNameId().withNameQualifier("").withSpNameQualifier(hubEntityId).build();

        SubjectConfirmationDataBuilder subjectConfirmationDataBuilder = aSubjectConfirmationData().withInResponseTo(requestId);
        assertions.stream().forEach(subjectConfirmationDataBuilder::addAssertion);

        final SubjectConfirmationData subjectConfirmationData = subjectConfirmationDataBuilder.build();
        final SubjectConfirmation subjectConfirmation = SubjectConfirmationBuilder.aSubjectConfirmation()
                .withSubjectConfirmationData(subjectConfirmationData).build();

        return SubjectBuilder.aSubject().withNameId(nameId).withSubjectConfirmation(subjectConfirmation).build();
    }

    public static Assertion aMatchingDatasetAssertion(List<Attribute> attributes, boolean shouldBeExpired, String requestId) {
        return aMatchingDatasetAssertionWithSignature(attributes, aValidSignature(), shouldBeExpired, requestId);
    }

    public static Assertion aMatchingDatasetAssertionWithSignature(List<Attribute> attributes, Signature signature, boolean shouldBeExpired, String requestId) {
        return anAssertion()
                .withId("mds-assertion")
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                .withSubject(
                        anAssertionSubject(requestId, shouldBeExpired)
                )
                .withSignature(signature)
                .addAttributeStatement(
                        anAttributeStatement()
                                .addAllAttributes(attributes)
                                .build()
                ).buildUnencrypted();
    }

    public static EncryptedAssertion anEidasEncryptedAssertion(String issuerId) {
        return anAssertion()
                .addAuthnStatement(AuthnStatementBuilder.anAuthnStatement().build())
                .withIssuer(
                        anIssuer()
                                .withIssuerId(issuerId)
                                .build())
                .withSignature(aValidSignature())
                .withConditions(aConditions())
                .buildWithEncrypterCredential(
                        new TestCredentialFactory(
                                TEST_RP_MS_PUBLIC_ENCRYPTION_CERT,
                                TEST_RP_MS_PRIVATE_ENCRYPTION_KEY
                        ).getEncryptingCredential()
                );
    }

    public static EncryptedAssertion anEidasEncryptedAssertionWithInvalidSignature() {
        return anAssertion()
            .withIssuer(
                anIssuer()
                    .withIssuerId(STUB_IDP_ONE)
                    .build())
            .withSignature(aSignature()
                .withSigningCredential(
                    new TestCredentialFactory(
                        TEST_RP_PUBLIC_SIGNING_CERT,
                        TEST_RP_PRIVATE_SIGNING_KEY
                    ).getSigningCredential()
                ).build())
            .buildWithEncrypterCredential(
                new TestCredentialFactory(
                    TEST_RP_MS_PUBLIC_ENCRYPTION_CERT,
                    TEST_RP_MS_PRIVATE_ENCRYPTION_KEY
                ).getEncryptingCredential()
            );
    }

    private static Subject anAssertionSubject(final String inResponseTo, boolean shouldBeExpired) {
        final DateTime notOnOrAfter;
        if (shouldBeExpired) {
            notOnOrAfter = DateTime.now();
        } else {
            notOnOrAfter = DateTime.now().plus(1000000);
        }
        return SubjectBuilder.aSubject()
                .withSubjectConfirmation(
                        aSubjectConfirmation()
                                .withSubjectConfirmationData(
                                        aSubjectConfirmationData()
                                                .withNotOnOrAfter(notOnOrAfter)
                                                .withInResponseTo(inResponseTo)
                                                .build()
                                ).build()
                ).build();
    }

    private static Conditions aConditions() {
        Conditions conditions = new ConditionsBuilder().buildObject();
        conditions.setNotBefore(DateTime.now());
        conditions.setNotOnOrAfter(DateTime.now().plusMinutes(10));
        AudienceRestriction audienceRestriction= new AudienceRestrictionBuilder().buildObject();
        Audience audience = new AudienceBuilder().buildObject();
        audience.setAudienceURI(HUB_SECONDARY_ENTITY_ID);
        audienceRestriction.getAudiences().add(audience);
        conditions.getAudienceRestrictions().add(audienceRestriction);
        return conditions;
    }
}
