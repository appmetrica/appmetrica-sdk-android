package io.appmetrica.analytics.ecommerce;

import io.appmetrica.analytics.assertions.ObjectPropertyAssertions;
import io.appmetrica.analytics.testutils.CommonTest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ECommerceAmountTest extends CommonTest {

    private Object inputAmount;
    private String inputUnit;
    private String expected;

    public ECommerceAmountTest(Object inputAmount, String inputUnit, String expected, String description) {
        this.inputAmount = inputAmount;
        this.inputUnit = inputUnit;
        this.expected = expected;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "#{index}: {3}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {1000000L, "USD", "1", "Simple micros"},
                {-1000000L, "EUR", "-1", "Simple negative micros"},
                {0L, "BYN", "0", "Zero micros"},
                {1L, "USD", "0.000001", "Small micros"},
                {-1L, "USD", "-0.000001", "Small negative micros"},
                {1000000000000000001L, "USD", "1000000000000.000001", "High precision micros"},
                {-1000000000000000001L, "USD", "-1000000000000.000001", "Negative high precision micros"},
                {Long.MAX_VALUE, "USD", "9223372036854.775807", "Max long"},
                {Long.MIN_VALUE, "USD", "-9223372036854.775808", "Min long"},
                {0d, "USD", "0", "Zero double"},
                {1d, "USD", "1", "Simple integer double"},
                {-1d, "USD", "-1", "Simple negative integer double"},
                {100000000000000d, "USD", "100000000000000", "Large double"},
                {-100000000000000d, "USD", "-100000000000000", "Large negative double"},
                {1000000000000001d, "BYN", "1000000000000001", "Large high precision double"},
                {-1000000000000001d, "BYN", "-1000000000000001", "Large negative high precision double"},
                {0.000000000000000001d, "EUR", "0.000000000000000001", "Small double"},
                {-0.000000000000000001d, "EUR", "-0.000000000000000001", "Small negative double"},
                {10000.00000000000000001d, "RUB", "10000.00000000000000001", "Small high precision double"},
                {-10000.00000000000000001d, "RUB", "-10000.00000000000000001", "Small high precision negative double"},
                {Double.MAX_VALUE, "RUB", String.valueOf(Double.MAX_VALUE), "Max double"},
                {-Double.MAX_VALUE, "RUB", String.valueOf(-Double.MAX_VALUE), "Negative max double"},
                {Double.MIN_VALUE, "RUB", String.valueOf(Double.MIN_VALUE), "Min double"},
                {-Double.MIN_VALUE, "RUB", String.valueOf(-Double.MIN_VALUE), "Negative min double"},
                {Double.MIN_NORMAL, "RUB", String.valueOf(Double.MIN_NORMAL), "Min normal double"},
                {-Double.MIN_NORMAL, "RUB", String.valueOf(-Double.MIN_NORMAL), "Negative min normal double"},
                {Double.NaN, "USD", "0", "NaN double"},
                {Double.NEGATIVE_INFINITY, "USD", "0", "Negative infinity double"},
                {Double.POSITIVE_INFINITY, "USD", "0", "Positive infinity double"},
                {BigDecimal.ZERO, "USD", "0", "Zero BigDecimal"},
                {BigDecimal.ONE, "USD", "1", "One BigDecimal"},
                {BigDecimal.ONE.negate(), "USD", "-1", "Negative one BigDecimal"},
                {BigDecimal.TEN.pow(20), "USD", "100000000000000000000", "Huge BigDecimal"},
                {BigDecimal.TEN.pow(20).negate(), "USD", "-100000000000000000000", "Huge negative BigDecimal"},
                {
                        BigDecimal.ONE.divide(BigDecimal.TEN.pow(20), 20, RoundingMode.HALF_UP),
                        "USD",
                        "0.00000000000000000001",
                        "Small BigDecimal"
                },
                {
                        BigDecimal.ONE.divide(BigDecimal.TEN.pow(20), 20, RoundingMode.HALF_UP).negate(),
                        "USD",
                        "-0.00000000000000000001",
                        "Small negative BigDecimal"
                },
                {
                        BigDecimal.valueOf(Double.MAX_VALUE).multiply(BigDecimal.TEN),
                        "USD",
                        "1.79769313486231570E+309",
                        "Big decimal bigger than max double"
                },
                {
                        BigDecimal.valueOf(Double.MIN_VALUE).divide(BigDecimal.TEN, RoundingMode.HALF_UP),
                        "USD",
                        "5E-325",
                        "Big decimal smaller than min double"
                },
                {
                        BigDecimal.valueOf(-Double.MAX_VALUE).multiply(BigDecimal.TEN),
                        "USD",
                        "-1.79769313486231570E+309",
                        "Big decimal smaller than negative max double"
                },
                {
                        BigDecimal.valueOf(-Double.MIN_VALUE).divide(BigDecimal.TEN, BigDecimal.ROUND_HALF_UP),
                        "USD",
                        "-5E-325",
                        "Big decimal bigger than negative min double"
                }
        });
    }

    private ECommerceAmount actual;

    @Before
    public void setUp() throws Exception {
        if (inputAmount instanceof Long) {
            actual = new ECommerceAmount((Long) inputAmount, inputUnit);
        } else if (inputAmount instanceof Double) {
            actual = new ECommerceAmount((Double) inputAmount, inputUnit);
        } else if (inputAmount instanceof BigDecimal) {
            actual = new ECommerceAmount((BigDecimal) inputAmount, inputUnit);
        } else {
            throw new IllegalArgumentException(String.format("Unexpected inputAmount = %s", inputAmount));
        }
    }

    @Test
    public void constructor() throws Exception {
        ObjectPropertyAssertions<ECommerceAmount> assertions = ObjectPropertyAssertions(actual)
                .withDeclaredAccessibleFields(true);

        assertions.checkDecimalField("amount", "getAmount", new BigDecimal(expected), 0.0001d);
        assertions.checkField("unit", "getUnit", inputUnit);

        assertions.checkAll();
    }
}
