package uk.gov.ida.verifymatchingservicetesttool.configurations;

import org.junit.Rule;
import org.junit.jupiter.api.Test;
import org.junit.rules.ExpectedException;
import uk.gov.ida.verifymatchingservicetesttool.exceptions.MsaTestingToolConfigException;

import java.io.File;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ConfigurationReaderTest {

    @Rule
    private final ExpectedException exception = ExpectedException.none();

    @Test
    void shouldThrowExceptionWhenConfigFileDoesNotExist() throws MsaTestingToolConfigException {
        ConfigurationReader configurationReader = mock(ConfigurationReader.class);
        when(configurationReader.getConfiguration(any())).thenCallRealMethod();
        File mock = mock(File.class);
        when(configurationReader.getAbsoluteFilePath(any())).thenReturn(mock);

        when(mock.exists()).thenReturn(false);
        exception.expect(MsaTestingToolConfigException.class);

        configurationReader.getAbsoluteFilePath("notarealfile");
    }
}
