package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.TestUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SdkUtilsTest extends CommonTest {

    @Test
    public void testOnlyMetricaCrash() {
        Throwable throwable = TestUtils.createThrowableMock("at com.android.blabla\nat io.appmetrica.analytics.BlaBla\nat com.android.blabla");
        assertThat(SdkUtils.isExceptionFromMetrica(throwable)).isTrue();
        assertThat(SdkUtils.isExceptionFromPushSdk(throwable)).isFalse();
    }

    @Test
    public void testOnlyPushCrash() {
        Throwable throwable = TestUtils.createThrowableMock("at com.android.blabla\nat io.appmetrica.analytics.push.BlaBla\nat com.android.blabla");
        assertThat(SdkUtils.isExceptionFromPushSdk(throwable)).isTrue();
        assertThat(SdkUtils.isExceptionFromMetrica(throwable)).isFalse();
    }

    @Test
    public void testOnlyPushWithSuffixCrash() {
        Throwable throwable = TestUtils.createThrowableMock("at com.android.blabla\nat io.appmetrica.analytics.pushsuffix.BlaBla\nat com.android.blabla");
        assertThat(SdkUtils.isExceptionFromPushSdk(throwable)).isTrue();
        assertThat(SdkUtils.isExceptionFromMetrica(throwable)).isFalse();
    }

    @Test
    public void testMetricaAndPushCrash() {
        Throwable throwable = TestUtils.createThrowableMock("at com.android.blabla\nat io.appmetrica.analytics.BlaBla\nat io.appmetrica.analytics.push.BlaBla");
        assertThat(SdkUtils.isExceptionFromMetrica(throwable)).isTrue();
        assertThat(SdkUtils.isExceptionFromPushSdk(throwable)).isTrue();
    }

    @Test
    public void testNonSdkCrash() {
        Throwable throwable = TestUtils.createThrowableMock("at com.android.blabla\nat com.yandex.browser.push.BlaBla\nat com.android.blabla");
        assertThat(SdkUtils.isExceptionFromMetrica(throwable)).isFalse();
        assertThat(SdkUtils.isExceptionFromPushSdk(throwable)).isFalse();
    }
}
