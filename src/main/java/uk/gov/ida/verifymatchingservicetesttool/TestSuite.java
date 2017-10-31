package uk.gov.ida.verifymatchingservicetesttool;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.gov.ida.verifymatchingservicetesttool.tests.Maths;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    Maths.class
})
public class TestSuite {
}
