package uk.gov.ida.matchingserviceadapter.rest.configuration.verification;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;
import uk.gov.ida.common.shared.security.X509CertificateFactory;
import uk.gov.ida.common.shared.security.verification.CertificateChainValidator;
import uk.gov.ida.common.shared.security.verification.PKIXParametersProvider;
import uk.gov.ida.common.shared.security.verification.exceptions.CertificateChainValidationException;

import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.fail;

@RunWith(MockitoJUnitRunner.class)
public class FixedCertificateChainValidatorTest {
    private X509CertificateFactory certificateFactory;
    private FixedCertificateChainValidator certificateChainValidator;

    @Before
    public void setUp() throws Exception {
        certificateFactory = new X509CertificateFactory();
        certificateChainValidator = new FixedCertificateChainValidator(
                getTrustStore(),
                new CertificateChainValidator(new PKIXParametersProvider(), new X509CertificateFactory()));
    }

    @Test
    public void validate_shouldPassACertSignedByRootCACertInTrustStore() throws Exception {
        final X509Certificate intermediaryCACertificate = certificateFactory.createCertificate(caCert);

        certificateChainValidator.validate(intermediaryCACertificate);
    }

    @Test
    public void validate_shouldPassACertSignedByAnIntermediaryCACertSignedByRootCACertInTrustStore() throws Exception {
        final X509Certificate encryptionCertificate = certificateFactory.createCertificate(this.trustedCertString);

        certificateChainValidator.validate(encryptionCertificate);
    }

    @Test
    public void validate_shouldFailACertSignedByAnUnknownRootCACert() throws Exception {
        final X509Certificate otherChildCertificate =
                certificateFactory.createCertificate(childSignedByOtherRootCAString);

        assertExceptionMessage(
                certificateChainValidator,
                otherChildCertificate,
                CertificateChainValidationException.class,
                "Certificate is not valid: O=other_server, CN=localhost"
        );
    }

    private void assertExceptionMessage(
            FixedCertificateChainValidator validator,
            X509Certificate certificate,
            Class exceptionClass,
            String value) {

        try {
            validator.validate(certificate);
        } catch (Exception e) {
            assertThat(e.getClass()).isEqualTo(exceptionClass);
            assertThat(e.getMessage()).isEqualTo(value);
            return;
        }
        fail("Should have thrown exception.");
    }

    public KeyStore getTrustStore() throws CertificateException, NoSuchAlgorithmException, IOException, KeyStoreException {
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(null, null);
        X509Certificate certificate = new X509CertificateFactory().createCertificate(caCert);
        ks.setCertificateEntry("CA", certificate);
        return ks;
    }

    private final String caCert = "-----BEGIN CERTIFICATE-----\n" +
            "MIICyDCCAbCgAwIBAgIJAJmsCyNdn2hMMA0GCSqGSIb3DQEBBQUAMBMxETAPBgNV\n" +
            "BAMMCE15VGVzdENBMCAXDTE1MDgxNzA4MzgzMFoYDzIxMTUwNzI0MDgzODMwWjAT\n" +
            "MREwDwYDVQQDDAhNeVRlc3RDQTCCASIwDQYJKoZIhvcNAQEBBQADggEPADCCAQoC\n" +
            "ggEBALtR8OLWpW9aKh5GYVmyBo+ZGr4AjyhFlf7pa9rhG9K6fvNnBllUbcQhBv+r\n" +
            "34YwIZ50mWKVuD5Jlb8Ad6506vWq1EX1WXUIB36ZdCRx26zUquAtYi448ULKoN4k\n" +
            "qVRpsmsfguBC/u7EvWoD+ImsKiGwm2+Q0d87z0ImkkVTf6100MMyR799WSEaqXuS\n" +
            "sW1CG3Oerhnj4FvMgs1ISX+5jdZjwzvkKIxGWPtKTuXfcXKCrV/+spuQFffcYRkG\n" +
            "emtxJPoaM/BuA7sDzwP0ASZNr6vtFoeQGhCk7q+qjTKkRzQB949F0O2K3KqOlCI0\n" +
            "x5J/VnRiIPVCbJJZtYv6DO+Y15ECAwEAAaMdMBswDAYDVR0TBAUwAwEB/zALBgNV\n" +
            "HQ8EBAMCAQYwDQYJKoZIhvcNAQEFBQADggEBAFvp1PSiDfVbFbTdX1Zil35vyq5t\n" +
            "VmEJyRF2hHOY+RSpJqezDoP7SuDtf3Ui7lS+yzzt3W/ZbsqOHJdMrDT7y7yWyEh8\n" +
            "hJGYo7MI2Ih9+nlsF8mw6G/vxs7wERTEs5OGpVZeIEW3crSlSLObUVrIwuHJxPOR\n" +
            "ovDT3w6hVCE0hzbu+4y38TRTyhVgLj0cFyCIhs52BOCuB2JjeW88UQCKp65wHZBk\n" +
            "Ne0xXBtsfhFc1dW3Mnvix89nrv6cVCxnqO3lMOQGzP11XXrMYJmvdRKD8/z10HnO\n" +
            "Om7P4NDY2IIGbaPzlJlbmZgVKWg0nBXSOjAEA6Q9JCri+rjbZEJGldUyTPI=\n" +
            "-----END CERTIFICATE-----\n";

    private final String trustedCertString = "-----BEGIN CERTIFICATE-----\n" +
            "MIIC5DCCAcygAwIBAgIBBTANBgkqhkiG9w0BAQsFADATMREwDwYDVQQDDAhNeVRl\n" +
            "c3RDQTAgFw0xNTA4MTcxMTE3MDBaGA8yMTE1MDcyNDExMTcwMFowJTESMBAGA1UE\n" +
            "AxMJbG9jYWxob3N0MQ8wDQYDVQQKEwZzZXJ2ZXIwggEiMA0GCSqGSIb3DQEBAQUA\n" +
            "A4IBDwAwggEKAoIBAQDCjm8RzlK7XneBfDmrqsnqGcu7yA5YgF73HEFX0FHghyb7\n" +
            "hX9Hy9C11sXVC5a1Bu0kv60Jnl5/WgZg3+CvQAubIEtQF7CcWuBqa2f2R15cUPB4\n" +
            "QAWgQ5rGJkGY+RTB1cgsILog1K0R8wDocTqxqdQrO35oOSFwtU6uk2PurSKnKMVQ\n" +
            "APD/3mIG3U3yYAJUi8OpM25ICgrxOmTZ1C0nY/tM5CHLTd73UuJ1IEljYq7PYfdy\n" +
            "IWaYJCQdl0rnwOyUGti7kpmaLB4ypLZETz2gQFF6s8D8O2ed1pLaeR1Qns9av2Vl\n" +
            "i1mkfweJlS53adUekqhcYA6EoJv6BqNFMRk4WRBFAgMBAAGjLzAtMAkGA1UdEwQC\n" +
            "MAAwCwYDVR0PBAQDAgWgMBMGA1UdJQQMMAoGCCsGAQUFBwMBMA0GCSqGSIb3DQEB\n" +
            "CwUAA4IBAQAUfrhCQxfX2Gzd5Oa2weNpviDEyouConVVHx+3dJvLkh1kCFXBJgoe\n" +
            "kt7BeV41HcXv7KU1I+v/9admXlTVndKyO+AOOvfj75A0x6BBcRF2hMtnANNP3voW\n" +
            "CVQTTAwFJtddCoqM/wAf9AieO0I9rtKHDSjuZ1LL9Tb1qGlsKqnOyr0I/bkVLOfp\n" +
            "BDFfenS08kJqhFfV7e56yYhc1fM26CmzXBfvZFjcHMBMWzISZ+ss8/q1RVFEGkdi\n" +
            "Ejp/JdzKioT+3Ctkd4taI4ccdRA2jCWUiFXEXRhdvxcLlX8XwqxEbKT5nYASva0P\n" +
            "bIBwAaTnGn53cXxMuHz1KxHcdbxOFkaf\n" +
            "-----END CERTIFICATE-----\n";

    private final String childSignedByOtherRootCAString = "-----BEGIN CERTIFICATE-----\n"+
            "MIIC7TCCAdWgAwIBAgIBAjANBgkqhkiG9w0BAQUFADAWMRQwEgYDVQQDDAtPdGhl\n"+
            "clRlc3RDQTAgFw0xNTA5MTUxMTE1NDVaGA8yMTE1MDgyMjExMTU0NVowKzESMBAG\n"+
            "A1UEAxMJbG9jYWxob3N0MRUwEwYDVQQKFAxvdGhlcl9zZXJ2ZXIwggEiMA0GCSqG\n"+
            "SIb3DQEBAQUAA4IBDwAwggEKAoIBAQDCjm8RzlK7XneBfDmrqsnqGcu7yA5YgF73\n"+
            "HEFX0FHghyb7hX9Hy9C11sXVC5a1Bu0kv60Jnl5/WgZg3+CvQAubIEtQF7CcWuBq\n"+
            "a2f2R15cUPB4QAWgQ5rGJkGY+RTB1cgsILog1K0R8wDocTqxqdQrO35oOSFwtU6u\n"+
            "k2PurSKnKMVQAPD/3mIG3U3yYAJUi8OpM25ICgrxOmTZ1C0nY/tM5CHLTd73UuJ1\n"+
            "IEljYq7PYfdyIWaYJCQdl0rnwOyUGti7kpmaLB4ypLZETz2gQFF6s8D8O2ed1pLa\n"+
            "eR1Qns9av2Vli1mkfweJlS53adUekqhcYA6EoJv6BqNFMRk4WRBFAgMBAAGjLzAt\n"+
            "MAkGA1UdEwQCMAAwCwYDVR0PBAQDAgWgMBMGA1UdJQQMMAoGCCsGAQUFBwMBMA0G\n"+
            "CSqGSIb3DQEBBQUAA4IBAQCGfTcqLiqr4fmm/iHbvT+Qgr1YjN9td9BK30D031Rb\n"+
            "LItxSR5dAgC+L9A3n/o1wWszqF2kxl9AMRfQfIILNflJ4ED4ymrsAtN2VdWlhv7y\n"+
            "io4LT49CFc7BF+EYj07zT6zu0gDCLBHbH3Ah1Iack82UHqZfjMKB4pw0FeoKOJ72\n"+
            "kNji93SKj6jh5hPQXd678+I2GQvqLzZ6FGQ681O6FMVknDDkXuKdC49VTsw13iK7\n"+
            "3OPERq/ptqpZIeYJ9yeGYGdKXZpnA8kElSrXIISvjNZrGAbXmwtn5437m+4O6Wpy\n"+
            "2rx8g1cBTmbLe2MBPY3pvPAXJ33Tk6fwtNqD4Eh6mS1Q\n"+
            "-----END CERTIFICATE-----\n";
}
