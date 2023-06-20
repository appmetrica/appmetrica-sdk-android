package io.appmetrica.analytics;

import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class ReceiptTest extends CommonTest {

    @Test
    public void testOptional() {
        Revenue.Receipt receipt = Revenue.Receipt.newBuilder()
                .withData("data")
                .withSignature("signature").build();
        SoftAssertions softAssertion = new SoftAssertions();
        softAssertion.assertThat(receipt.data).as("data").isEqualTo("data");
        softAssertion.assertThat(receipt.signature).as("signature").isEqualTo("signature");
        softAssertion.assertAll();
    }

}
