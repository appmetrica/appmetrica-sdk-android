package io.appmetrica.analytics.billinginterface.internal.config;

import io.appmetrica.analytics.billinginterface.internal.Period;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class PeriodTest {

    private final String period;
    private final Period expected;

    public PeriodTest(final String period,
                      final Period expected) {
        this.period = period;
        this.expected = expected;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {
                    "P1D", new Period(1, Period.TimeUnit.DAY)
                },
                {
                    "P1W", new Period(1, Period.TimeUnit.WEEK)
                },
                {
                    "P1M", new Period(1, Period.TimeUnit.MONTH)
                },
                {
                    "P1Y", new Period(1, Period.TimeUnit.YEAR)
                },
                {
                    "P120W", new Period(120, Period.TimeUnit.WEEK)
                },
                {
                    "P1", null
                },
                {
                    "Q1W", null
                },
                {
                    "text", null
                },
        });
    }

    @Test
    public void testParse() {
        assertThat(Period.parse(period)).isEqualTo(expected);
    }
}
