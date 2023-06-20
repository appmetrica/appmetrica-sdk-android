package io.appmetrica.analytics.impl;

import io.appmetrica.analytics.AppMetrica;
import io.appmetrica.analytics.BuildConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class SdkDataTest extends CommonTest {

    @Test
    public void testCurrentEqualsApiLevelFromAppMetrica() {
        assertThat(SdkData.CURRENT).isEqualTo(AppMetrica.getLibraryApiLevel());
    }

    @Test
    public void testCurrentEqualsApiLevelFromBuildConfig() {
        assertThat(SdkData.CURRENT).isEqualTo(BuildConfig.API_LEVEL);
    }

    @Test
    public void testCurrentVersionNameEqualsVersionFromBuildConfig() {
        assertThat(SdkData.CURRENT_VERSION).isEqualTo(BuildConfig.VERSION_NAME);
    }

    @Test
    public void testCurrentVersionNameEqualsVersionFromAppMetrica() {
        assertThat(SdkData.CURRENT_VERSION).isEqualTo(AppMetrica.getLibraryVersion());
    }

    @Test
    public void testCurrentVersionForMappingEqualsVersionFromAppMetrica() {
        assertThat(AppMetrica.getLibraryVersion()).contains(SdkData.CURRENT_VERSION_NAME_FOR_MAPPING);
    }

    @Test
    public void testCurrentVersionForMappingEqualsVersionFromBuildConfig() {
        assertThat(BuildConfig.VERSION_NAME).contains(SdkData.CURRENT_VERSION_NAME_FOR_MAPPING);
    }
}
