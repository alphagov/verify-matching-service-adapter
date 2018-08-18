package uk.gov.ida.matchingserviceadapter;

import org.junit.Test;
import uk.gov.ida.common.shared.security.Certificate;

import static org.assertj.core.api.Assertions.assertThat;

public class MatchingServiceAdapterModuleTest {

    private static final String CERT = "-----BEGIN CERTIFICATE-----\nMIIBGzCBxgIJAL0noY5tc8OPMA0GCSqGSIb3DQEBCwUAMBUxEzARBgNVBAMMCnNl\nbGZzaWduZWQwHhcNMTgwODIzMDY1MzM2WhcNMTkwODIzMDY1MzM2WjAVMRMwEQYD\nVQQDDApzZWxmc2lnbmVkMFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMS856cUwkeE\nrqtE+IyfzSFHECkKsOw35xQTNo3u32IjbwzykzOC2x+Pvyh47U3DXM52wPzi3uiL\n+GB4WOtEL0cCAwEAATANBgkqhkiG9w0BAQsFAANBAIIrGyaQCLIqCutaICJbdbIN\nmUzVkrY1iFLRVrfSZ37Ush1sxqpr/YHRf+apHMRXHlITuBrU8HIZbYEiaJUP718=\n-----END CERTIFICATE-----";

    @Test
    public void testCertLoading() {
        MatchingServiceAdapterModule matchingServiceAdapterModule = new MatchingServiceAdapterModule();
        final Certificate certificate = matchingServiceAdapterModule.cert("test cert", CERT, uk.gov.ida.common.shared.security.Certificate.KeyUse.Signing);
        assertThat(certificate.getCertificate()).isEqualTo("MIIBGzCBxgIJAL0noY5tc8OPMA0GCSqGSIb3DQEBCwUAMBUxEzARBgNVBAMMCnNlbGZzaWduZWQwHhcNMTgwODIzMDY1MzM2WhcNMTkwODIzMDY1MzM2WjAVMRMwEQYDVQQDDApzZWxmc2lnbmVkMFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMS856cUwkeErqtE+IyfzSFHECkKsOw35xQTNo3u32IjbwzykzOC2x+Pvyh47U3DXM52wPzi3uiL+GB4WOtEL0cCAwEAATANBgkqhkiG9w0BAQsFAANBAIIrGyaQCLIqCutaICJbdbINmUzVkrY1iFLRVrfSZ37Ush1sxqpr/YHRf+apHMRXHlITuBrU8HIZbYEiaJUP718=");

        final Certificate certificateWithText = matchingServiceAdapterModule.cert("test cert", "Some text before the cert\n" + CERT, uk.gov.ida.common.shared.security.Certificate.KeyUse.Signing);
        assertThat(certificateWithText.getCertificate()).isEqualTo("MIIBGzCBxgIJAL0noY5tc8OPMA0GCSqGSIb3DQEBCwUAMBUxEzARBgNVBAMMCnNlbGZzaWduZWQwHhcNMTgwODIzMDY1MzM2WhcNMTkwODIzMDY1MzM2WjAVMRMwEQYDVQQDDApzZWxmc2lnbmVkMFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAMS856cUwkeErqtE+IyfzSFHECkKsOw35xQTNo3u32IjbwzykzOC2x+Pvyh47U3DXM52wPzi3uiL+GB4WOtEL0cCAwEAATANBgkqhkiG9w0BAQsFAANBAIIrGyaQCLIqCutaICJbdbINmUzVkrY1iFLRVrfSZ37Ush1sxqpr/YHRf+apHMRXHlITuBrU8HIZbYEiaJUP718=");
    }
}
