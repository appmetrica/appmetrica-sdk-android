package io.appmetrica.analytics.apphud.impl.config.service;

import android.os.Bundle;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.config.service.model.ServiceSideApphudConfig;
import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class ServiceSideApphudConfigToBundleConverterTest extends CommonTest {

    @NonNull
    private final ServiceSideApphudConfigToBundleConverter converter = new ServiceSideApphudConfigToBundleConverter();

    @Test
    public void convertIfEnabled() {
        String apiKey = "some_api_key";
        ServiceSideApphudConfig config = new ServiceSideApphudConfig(
            true,
            apiKey
        );
        Bundle bundle = converter.convert(config);

        assertThat(bundle).isNotNull();
        assertThat(bundle.containsKey(Constants.ServiceConfig.ENABLED_KEY)).isTrue();
        assertThat(bundle.getBoolean(Constants.ServiceConfig.ENABLED_KEY)).isTrue();
        assertThat(bundle.containsKey(Constants.ServiceConfig.API_KEY_KEY)).isTrue();
        assertThat(bundle.getString(Constants.ServiceConfig.API_KEY_KEY)).isEqualTo(apiKey);
    }

    @Test
    public void convertIfDisabled() {
        String apiKey = "some_api_key";
        ServiceSideApphudConfig config = new ServiceSideApphudConfig(
            false,
            apiKey
        );
        Bundle bundle = converter.convert(config);

        assertThat(bundle).isNotNull();
        assertThat(bundle.containsKey(Constants.ServiceConfig.ENABLED_KEY)).isTrue();
        assertThat(bundle.getBoolean(Constants.ServiceConfig.ENABLED_KEY)).isFalse();
        assertThat(bundle.containsKey(Constants.ServiceConfig.API_KEY_KEY)).isTrue();
        assertThat(bundle.getString(Constants.ServiceConfig.API_KEY_KEY)).isEqualTo(apiKey);
    }

    @Test
    public void convertNull() {
        Bundle bundle = converter.convert(null);

        assertThat(bundle).isNull();
    }
}
