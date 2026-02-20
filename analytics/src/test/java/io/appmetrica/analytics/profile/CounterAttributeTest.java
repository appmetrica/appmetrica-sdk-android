package io.appmetrica.analytics.profile;

import io.appmetrica.analytics.impl.profile.CounterUpdatePatcher;
import io.appmetrica.analytics.impl.profile.KeyValidator;
import io.appmetrica.analytics.testutils.CommonTest;
import org.assertj.core.api.SoftAssertions;
import org.junit.Test;

public class CounterAttributeTest extends CommonTest {

    private static final String KEY = "counterKey";
    private static final int VALUE = 200;

    @Test
    public void testCounterAttribute() {
        CounterUpdatePatcher patcher = (CounterUpdatePatcher) Attribute.customCounter(KEY).withDelta(VALUE).getUserProfileUpdatePatcher();
        SoftAssertions softAssertions = new SoftAssertions();
        softAssertions.assertThat(patcher.getKey()).as("key").isEqualTo(KEY);
        softAssertions.assertThat(patcher.getValue()).as("value").isEqualTo(VALUE);
        softAssertions.assertThat(patcher.getKeyValidator()).as("key validator").isExactlyInstanceOf(KeyValidator.class);
        softAssertions.assertAll();
    }

}
