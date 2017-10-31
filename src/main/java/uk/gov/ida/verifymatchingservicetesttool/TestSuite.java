package uk.gov.ida.verifymatchingservicetesttool;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.gov.ida.verifymatchingservicetesttool.tests.BasicMatchingTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    BasicMatchingTests.class
})
public class TestSuite {
}
