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
import uk.gov.ida.saml.core.extensions.IdaAuthnContext;
import uk.gov.ida.saml.core.test.TestCredentialFactory;
import uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder;
import uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder;
import uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder;

import java.util.List;

import static java.util.Arrays.asList;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PRIVATE_ENCRYPTION_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_MS_PUBLIC_ENCRYPTION_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.TEST_RP_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PUBLIC_SIGNING_CERT;
import static uk.gov.ida.saml.core.test.TestCertificateStrings.HUB_TEST_PRIVATE_SIGNING_KEY;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.HUB_SECONDARY_ENTITY_ID;
import static uk.gov.ida.saml.core.test.TestEntityIds.STUB_IDP_ONE;
import static uk.gov.ida.saml.core.test.builders.AssertionBuilder.anAssertion;
import static uk.gov.ida.saml.core.test.builders.AttributeStatementBuilder.anAttributeStatement;
import static uk.gov.ida.saml.core.test.builders.SimpleStringAttributeBuilder.aSimpleStringAttribute;
import static uk.gov.ida.saml.core.test.builders.AuthnContextBuilder.anAuthnContext;
import static uk.gov.ida.saml.core.test.builders.AuthnContextClassRefBuilder.anAuthnContextClassRef;
import static uk.gov.ida.saml.core.test.builders.AuthnStatementBuilder.anAuthnStatement;
import static uk.gov.ida.saml.core.test.builders.IPAddressAttributeBuilder.anIPAddress;
import static uk.gov.ida.saml.core.test.builders.IssuerBuilder.anIssuer;
import static uk.gov.ida.saml.core.test.builders.NameIdBuilder.aNameId;
import static uk.gov.ida.saml.core.test.builders.SignatureBuilder.aSignature;
import static uk.gov.ida.saml.core.test.builders.SubjectBuilder.aSubject;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationBuilder.aSubjectConfirmation;
import static uk.gov.ida.saml.core.test.builders.SubjectConfirmationDataBuilder.aSubjectConfirmationData;

public class AssertionHelper {

    private static Signature aValidSignature(String cert, String privateKey) {
        return aSignature()
                .withSigningCredential(
                        new TestCredentialFactory(
                                cert,
                                privateKey
                        ).getSigningCredential()
                ).build();
    }

    private static Signature aValidSignature() {
        return aValidSignature(STUB_IDP_PUBLIC_PRIMARY_CERT, STUB_IDP_PUBLIC_PRIMARY_PRIVATE_KEY);
    }

    public static Assertion anAuthnStatementAssertion() {
        return anAuthnStatementAssertion(IdaAuthnContext.LEVEL_2_AUTHN_CTX, "default-request-id");
    }

    public static Assertion anAuthnStatementAssertion(String authnContext, String inResponseTo) {
        return anAssertion()
                .addAuthnStatement(
                    anAuthnStatement()
                        .withAuthnContext(
                            anAuthnContext()
                                .withAuthnContextClassRef(
                                    anAuthnContextClassRef()
                                        .withAuthnContextClasRefValue(authnContext)
                                        .build())
                                .build())
                        .build())
                .withSubject(
                    aSubject()
                        .withSubjectConfirmation(
                            aSubjectConfirmation()
                                .withSubjectConfirmationData(
                                    aSubjectConfirmationData()
                                        .withInResponseTo(inResponseTo)
                                        .build()
                                ).build()
                        ).build())
                .withIssuer(anIssuer().withIssuerId(STUB_IDP_ONE).build())
                .addAttributeStatement(anAttributeStatement().addAttribute(anIPAddress().build()).build())
                .buildUnencrypted();
    }

    public static Subject aSubjectWithAssertions(List<Assertion> assertions, String requestId, String hubEntityId) {
        return aSubjectWithAssertions(assertions, requestId, hubEntityId, "default-pid");
    }

    public static Subject aSubjectWithAssertions(List<Assertion> assertions, String requestId, String hubEntityId, String pid) {
        final NameID nameId = aNameId().withValue(pid).withNameQualifier("").withSpNameQualifier(hubEntityId).build();
        SubjectConfirmationDataBuilder subjectConfirmationDataBuilder = aSubjectConfirmationData().withInResponseTo(requestId);
        assertions.stream().forEach(subjectConfirmationDataBuilder::addAssertion);
        final SubjectConfirmationData subjectConfirmationData = subjectConfirmationDataBuilder.build();
        final SubjectConfirmation subjectConfirmation = SubjectConfirmationBuilder.aSubjectConfirmation()
            .withSubjectConfirmationData(subjectConfirmationData).build();

        return aSubject().withNameId(nameId).withSubjectConfirmation(subjectConfirmation).build();
    }

    public static Subject aSubjectWithEncryptedAssertions(List<EncryptedAssertion> assertions, String requestId, String hubEntityId) {
        final NameID nameId = aNameId().withNameQualifier("").withSpNameQualifier(hubEntityId).build();

        SubjectConfirmationDataBuilder subjectConfirmationDataBuilder = aSubjectConfirmationData().withInResponseTo(requestId);
        assertions.stream().forEach(subjectConfirmationDataBuilder::addAssertion);

        final SubjectConfirmationData subjectConfirmationData = subjectConfirmationDataBuilder.build();
        final SubjectConfirmation subjectConfirmation = SubjectConfirmationBuilder.aSubjectConfirmation()
                .withSubjectConfirmationData(subjectConfirmationData).build();

        return aSubject().withNameId(nameId).withSubjectConfirmation(subjectConfirmation).build();
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

    public static Assertion aCycle3Assertion(String attributeName, String attributeValue, String requestId) {
        return anAssertion()
            .withId("cycle3-assertion")
            .withIssuer(anIssuer().withIssuerId(HUB_ENTITY_ID).build())
            .withSubject(
                anAssertionSubject(requestId, false)
            )
            .withSignature(aValidSignature(HUB_TEST_PUBLIC_SIGNING_CERT, HUB_TEST_PRIVATE_SIGNING_KEY))
            .addAttributeStatement(
                anAttributeStatement()
                    .addAllAttributes(asList(
                        aSimpleStringAttribute()
                            .withName(attributeName)
                            .withSimpleStringValue(attributeValue)
                            .build()
                    ))
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
        return aSubject()
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
