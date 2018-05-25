package feature.uk.gov.ida.verifymatchingservicetesttool;

import common.uk.gov.ida.verifymatchingservicetesttool.services.LocalMatchingServiceStub;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import uk.gov.ida.verifymatchingservicetesttool.Application;
import uk.gov.ida.verifymatchingservicetesttool.utils.ScenarioFilesLocator;
import uk.gov.ida.verifymatchingservicetesttool.utils.FilesLocator;

public abstract class FeatureTestBase {

    protected Application application;

    protected LocalMatchingServiceStub localMatchingService = new LocalMatchingServiceStub();
    protected SummaryGeneratingListener listener = new SummaryGeneratingListener();
    protected FilesLocator fileLocator = new ScenarioFilesLocator();

    @BeforeEach
    public void setUp() {
        application = new Application();
        localMatchingService.start();
    }

    @AfterEach
    public void tearDown() {
        localMatchingService.stop();
    }

}
