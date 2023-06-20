package io.appmetrica.analytics.impl;

import android.app.usage.UsageStatsManager;
import android.os.Build;
import io.appmetrica.analytics.testutils.CommonTest;
import java.util.Arrays;
import java.util.Collection;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.ParameterizedRobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;

public class AppStandbyBucketConverterTest extends CommonTest {

    @Config(sdk = Build.VERSION_CODES.R)
    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class AppStandbyBucketConverterRTest {

        private AppStandbyBucketConverter mConverter;
        private final int mAppStandbyBucketCode;
        private final BackgroundRestrictionsState.AppStandByBucket mAppStandbyBucket;
        private final String mAppStandbyBucketString;

        @ParameterizedRobolectricTestRunner.Parameters(name = "{1}")
        public static Collection<Object[]> data() {
            return Arrays.<Object[]>asList(
                    new Object[]{UsageStatsManager.STANDBY_BUCKET_RESTRICTED, BackgroundRestrictionsState.AppStandByBucket.RESTRICTED, "RESTRICTED"}
            );
        }

        public AppStandbyBucketConverterRTest(int appStandbyBucketCode,
                                              BackgroundRestrictionsState.AppStandByBucket appStandByBucket,
                                              String appStandbyBucketString) {
            mAppStandbyBucketCode = appStandbyBucketCode;
            mAppStandbyBucket = appStandByBucket;
            mAppStandbyBucketString = appStandbyBucketString;
            mConverter = new AppStandbyBucketConverter();
        }

        @Test
        public void testFromIntToAppStandbyBucket() {
            assertThat(mConverter.fromIntToAppStandbyBucket(mAppStandbyBucketCode)).isEqualTo(mAppStandbyBucket);
        }

        @Test
        public void testFromAppStandbyBucketToString() {
            assertThat(mConverter.fromAppStandbyBucketToString(mAppStandbyBucket)).isEqualTo(mAppStandbyBucketString);
        }

    }

    @Config(sdk = Build.VERSION_CODES.P)
    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class AppStandbyBucketConverterPTest {

        private AppStandbyBucketConverter mConverter;
        private final int mAppStandbyBucketCode;
        private final BackgroundRestrictionsState.AppStandByBucket mAppStandbyBucket;
        private final String mAppStandbyBucketString;

        @ParameterizedRobolectricTestRunner.Parameters(name = "{1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(
                    new Object[]{UsageStatsManager.STANDBY_BUCKET_ACTIVE, BackgroundRestrictionsState.AppStandByBucket.ACTIVE, "ACTIVE"},
                    new Object[]{UsageStatsManager.STANDBY_BUCKET_WORKING_SET, BackgroundRestrictionsState.AppStandByBucket.WORKING_SET, "WORKING_SET"},
                    new Object[]{UsageStatsManager.STANDBY_BUCKET_FREQUENT, BackgroundRestrictionsState.AppStandByBucket.FREQUENT, "FREQUENT"},
                    new Object[]{UsageStatsManager.STANDBY_BUCKET_RARE, BackgroundRestrictionsState.AppStandByBucket.RARE, "RARE"},
                    new Object[]{99, null, null});
        }

        public AppStandbyBucketConverterPTest(int appStandbyBucketCode,
                                              BackgroundRestrictionsState.AppStandByBucket appStandByBucket,
                                              String appStandbyBucketString) {
            mAppStandbyBucketCode = appStandbyBucketCode;
            mAppStandbyBucket = appStandByBucket;
            mAppStandbyBucketString = appStandbyBucketString;
            mConverter = new AppStandbyBucketConverter();
        }

        @Test
        public void testFromIntToAppStandbyBucket() {
            assertThat(mConverter.fromIntToAppStandbyBucket(mAppStandbyBucketCode)).isEqualTo(mAppStandbyBucket);
        }

        @Test
        public void testFromAppStandbyBucketToString() {
            assertThat(mConverter.fromAppStandbyBucketToString(mAppStandbyBucket)).isEqualTo(mAppStandbyBucketString);
        }

    }

    @Config(sdk = Build.VERSION_CODES.O)
    @RunWith(ParameterizedRobolectricTestRunner.class)
    public static class AppStandbyBucketConverterPrePTest {

        private AppStandbyBucketConverter mConverter;
        private final int mAppStandbyBucketCode;
        private final BackgroundRestrictionsState.AppStandByBucket mAppStandbyBucket;
        private final String mAppStandbyBucketString;

        @ParameterizedRobolectricTestRunner.Parameters(name = "{1}")
        public static Collection<Object[]> data() {
            return Arrays.asList(
                    new Object[]{UsageStatsManager.STANDBY_BUCKET_ACTIVE, null, null},
                    new Object[]{UsageStatsManager.STANDBY_BUCKET_WORKING_SET, null, null},
                    new Object[]{UsageStatsManager.STANDBY_BUCKET_FREQUENT, null, null},
                    new Object[]{UsageStatsManager.STANDBY_BUCKET_RARE, null, null},
                    new Object[]{UsageStatsManager.STANDBY_BUCKET_RESTRICTED, null, null},
                    new Object[]{99, null, null});
        }

        public AppStandbyBucketConverterPrePTest(int appStandbyBucketCode,
                                                 BackgroundRestrictionsState.AppStandByBucket appStandByBucket,
                                                 String appStandbyBucketString) {
            mAppStandbyBucketCode = appStandbyBucketCode;
            mAppStandbyBucket = appStandByBucket;
            mAppStandbyBucketString = appStandbyBucketString;
            mConverter = new AppStandbyBucketConverter();
        }

        @Test
        public void testFromIntToAppStandbyBucket() {
            assertThat(mConverter.fromIntToAppStandbyBucket(mAppStandbyBucketCode)).isEqualTo(mAppStandbyBucket);
        }

        @Test
        public void testFromAppStandbyBucketToString() {
            assertThat(mConverter.fromAppStandbyBucketToString(mAppStandbyBucket)).isEqualTo(mAppStandbyBucketString);
        }

    }
}
