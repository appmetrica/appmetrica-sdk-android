package io.appmetrica.analytics.billinginterface.internal.config;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;

@RunWith(RobolectricTestRunner.class)
public class BillingConfigTest {

    @Test
    public void constructor() throws Exception {
        final int sendFrequencySeconds = 73286;
        final int firstCollectingInappMaxAgeSeconds = 483756;
        BillingConfig config = new BillingConfig(sendFrequencySeconds, firstCollectingInappMaxAgeSeconds);
        ObjectPropertyAssertions(config)
                .checkField("sendFrequencySeconds", sendFrequencySeconds)
                .checkField("firstCollectingInappMaxAgeSeconds", firstCollectingInappMaxAgeSeconds)
                .checkAll();
    }
}
