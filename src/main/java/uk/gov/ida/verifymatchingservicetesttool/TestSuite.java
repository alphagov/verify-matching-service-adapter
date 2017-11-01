package uk.gov.ida.verifymatchingservicetesttool;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import uk.gov.ida.verifymatchingservicetesttool.tests.LevelOfAssuranceOneScenario;
import uk.gov.ida.verifymatchingservicetesttool.tests.LevelOfAssuranceTwoScenario;
import uk.gov.ida.verifymatchingservicetesttool.tests.OptionalAddressFieldsExcludedScenario;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    LevelOfAssuranceOneScenario.class,
    LevelOfAssuranceTwoScenario.class,
    OptionalAddressFieldsExcludedScenario.class
})
public class TestSuite {
}
