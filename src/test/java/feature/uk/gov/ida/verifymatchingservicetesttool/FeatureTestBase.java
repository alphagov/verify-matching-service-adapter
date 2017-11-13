package feature.uk.gov.ida.verifymatchingservicetesttool;

import common.uk.gov.ida.verifymatchingservicetesttool.services.LocalMatchingServiceStub;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import uk.gov.ida.verifymatchingservicetesttool.utils.TestStatusPrintingListener;

public abstract class FeatureTestBase {

    protected LocalMatchingServiceStub localMatchingService = new LocalMatchingServiceStub();
    protected TestStatusPrintingListener listener = new TestStatusPrintingListener();

    @BeforeEach
    public void setUp() {
        localMatchingService.start();
    }

    @AfterEach
    public void tearDown() {
        localMatchingService.stop();
    }

}
