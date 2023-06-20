package io.appmetrica.analytics.billingv3.impl;

import com.android.billingclient.api.BillingClient;
import io.appmetrica.analytics.billinginterface.internal.ProductType;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(ParameterizedRobolectricTestRunner.class)
public class ProductTypeParserTest {

    private final String productType;
    private final ProductType expected;

    public ProductTypeParserTest(final String productType,
                                 final ProductType expected) {
        this.productType = productType;
        this.expected = expected;
    }

    @ParameterizedRobolectricTestRunner.Parameters(name = "{0}")
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][]{
                {
                        BillingClient.SkuType.INAPP, ProductType.INAPP
                },
                {
                        BillingClient.SkuType.SUBS, ProductType.SUBS
                },
                {
                        "type", ProductType.UNKNOWN
                },
                {
                        "", ProductType.UNKNOWN
                },
        });
    }

    @Test
    public void testParse() {
        assertThat(ProductTypeParser.parse(productType)).isEqualTo(expected);
    }
}
