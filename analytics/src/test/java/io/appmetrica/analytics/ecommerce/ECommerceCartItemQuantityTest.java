package io.appmetrica.analytics.ecommerce;

import io.appmetrica.analytics.testutils.CommonTest;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.data.Percentage.withPercentage;

/**
 * @see ECommerceCartItemTest
 */
@RunWith(ParameterizedRobolectricTestRunner.class)
public class ECommerceCartItemQuantityTest extends CommonTest {

    private Object inputQuantity;
    private String expectedQuantity;

    public ECommerceCartItemQuantityTest(Object inputQuantity, String expectedQuantity, String description) {
        this.inputQuantity = inputQuantity;
        this.expectedQuantity = expectedQuantity;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "#{index}: {2}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {1000000L, "1", "Simple micros"},
                {-1000000L, "-1", "Simple negative micros"},
                {0L, "0", "Zero micros"},
                {1L, "0.000001", "Small micros"},
                {-1L, "-0.000001", "Small negative micros"},
                {1000000000000000001L, "1000000000000.000001", "High precision micros"},
                {-1000000000000000001L, "-1000000000000.000001", "Negative high precision micros"},
                {Long.MAX_VALUE, "9223372036854.775807", "Max long"},
                {Long.MIN_VALUE, "-9223372036854.775808", "Min long"},
                {0d, "0", "Zero double"},
                {1d, "1", "Simple integer double"},
                {-1d, "-1", "Simple negative integer double"},
                {100000000000000d, "100000000000000", "Large double"},
                {-100000000000000d, "-100000000000000", "Large negative double"},
                {1000000000000001d, "1000000000000001", "Large high precision double"},
                {-1000000000000001d, "-1000000000000001", "Large negative high precision double"},
                {0.000000000000000001d, "0.000000000000000001", "Small double"},
                {-0.000000000000000001d, "-0.000000000000000001", "Small negative double"},
                {10000.00000000000000001d, "10000.00000000000000001", "Small high precision double"},
                {-10000.00000000000000001d, "-10000.00000000000000001", "Small high precision negative double"},
                {Double.MAX_VALUE, String.valueOf(Double.MAX_VALUE), "Max double"},
                {-Double.MAX_VALUE, String.valueOf(-Double.MAX_VALUE), "Negative max double"},
                {Double.MIN_VALUE, String.valueOf(Double.MIN_VALUE), "Min double"},
                {-Double.MIN_VALUE, String.valueOf(-Double.MIN_VALUE), "Negative min double"},
                {Double.MIN_NORMAL, String.valueOf(Double.MIN_NORMAL), "Min normal double"},
                {-Double.MIN_NORMAL, String.valueOf(-Double.MIN_NORMAL), "Negative min normal double"},
                {Double.NaN, "0", "NaN double"},
                {Double.NEGATIVE_INFINITY, "0", "Negative infinity double"},
                {Double.POSITIVE_INFINITY, "0", "Positive infinity double"},
                {BigDecimal.ZERO, "0", "Zero BigDecimal"},
                {BigDecimal.ONE, "1", "One BigDecimal"},
                {BigDecimal.ONE.negate(), "-1", "Negative one BigDecimal"},
                {BigDecimal.TEN.pow(20), "100000000000000000000", "Huge BigDecimal"},
                {BigDecimal.TEN.pow(20).negate(), "-100000000000000000000", "Huge negative BigDecimal"},
                {
                        BigDecimal.ONE.divide(BigDecimal.TEN.pow(20), 20, RoundingMode.HALF_UP),
                        "0.00000000000000000001",
                        "Small BigDecimal"
                },
                {
                        BigDecimal.ONE.divide(BigDecimal.TEN.pow(20), 20, RoundingMode.HALF_UP).negate(),
                        "-0.00000000000000000001",
                        "Small negative BigDecimal"
                },
                {
                        BigDecimal.valueOf(Double.MAX_VALUE).multiply(BigDecimal.TEN),
                        "1.79769313486231570E+309",
                        "Big decimal bigger than max double"
                },
                {
                        BigDecimal.valueOf(Double.MIN_VALUE).divide(BigDecimal.TEN, RoundingMode.HALF_UP),
                        "5E-325",
                        "Big decimal smaller than min double"
                },
                {
                        BigDecimal.valueOf(-Double.MAX_VALUE).multiply(BigDecimal.TEN),
                        "-1.79769313486231570E+309",
                        "Big decimal smaller than negative max double"
                },
                {
                        BigDecimal.valueOf(-Double.MIN_VALUE).divide(BigDecimal.TEN, BigDecimal.ROUND_HALF_UP),
                        "-5E-325",
                        "Big decimal bigger than negative min double"
                }
        });
    }

    @Mock
    private ECommerceProduct product;
    @Mock
    private ECommercePrice revenue;

    private ECommerceCartItem actual;

    @Before
    public void setUp() throws Exception {
        MockitoAnnotations.openMocks(this);

        if (inputQuantity instanceof Long) {
            actual = new ECommerceCartItem(product, revenue, (Long) inputQuantity);
        } else if (inputQuantity instanceof Double) {
            actual = new ECommerceCartItem(product, revenue, (Double) inputQuantity);
        } else if (inputQuantity instanceof BigDecimal) {
            actual = new ECommerceCartItem(product, revenue, (BigDecimal) inputQuantity);
        } else {
            throw new IllegalArgumentException(String.format("Unexpected inputQuantity = %s", inputQuantity));
        }
    }

    @Test
    public void quantity() {
        assertThat(actual.getQuantity()).isCloseTo(new BigDecimal(expectedQuantity), withPercentage(0.0001d));
    }
}
