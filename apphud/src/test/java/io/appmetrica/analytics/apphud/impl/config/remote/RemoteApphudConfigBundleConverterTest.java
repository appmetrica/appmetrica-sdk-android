package io.appmetrica.analytics.apphud.impl.config.remote;

import android.os.Bundle;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class RemoteApphudConfigBundleConverterTest extends CommonTest {

    @NonNull
    private final RemoteApphudConfigBundleConverter converter = new RemoteApphudConfigBundleConverter();

    @Test
    public void convertIfEnabled() {
        String apiKey = "some_api_key";
        RemoteApphudConfig config = new RemoteApphudConfig(
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
        RemoteApphudConfig config = new RemoteApphudConfig(
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
