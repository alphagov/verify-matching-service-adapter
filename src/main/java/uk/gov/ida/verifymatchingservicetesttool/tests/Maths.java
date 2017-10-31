package uk.gov.ida.verifymatchingservicetesttool.tests;

import org.junit.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;

public class Maths {
    @Test
    public void shouldNotBreakMathematics() {
        assertThat(2+2, is(4));
    }
}
