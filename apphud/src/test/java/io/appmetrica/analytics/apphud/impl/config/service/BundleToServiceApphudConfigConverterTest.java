package io.appmetrica.analytics.apphud.impl.config.service;

import android.os.Bundle;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.testutils.CommonTest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;

import static org.assertj.core.api.Assertions.assertThat;

@RunWith(RobolectricTestRunner.class)
public class BundleToServiceApphudConfigConverterTest extends CommonTest {

    @NonNull
    private final BundleToServiceApphudConfigConverter parser = new BundleToServiceApphudConfigConverter();

    @Test
    public void parse() {
        String apiKey = "some_api_key";
        Bundle rawData = new Bundle();
        rawData.putBoolean(Constants.ServiceConfig.ENABLED_KEY, true);
        rawData.putString(Constants.ServiceConfig.API_KEY_KEY, apiKey);

        assertThat(parser.fromBundle(rawData)).usingRecursiveComparison().isEqualTo(new ServiceApphudConfig(
            true,
            apiKey
        ));
    }

    @Test
    public void parseIfNoEnabledField() {
        String apiKey = "some_api_key";
        Bundle rawData = new Bundle();
        rawData.putString(Constants.ServiceConfig.API_KEY_KEY, apiKey);

        assertThat(parser.fromBundle(rawData)).usingRecursiveComparison().isEqualTo(new ServiceApphudConfig(
            false,
            apiKey
        ));
    }

    @Test
    public void parseIfNoApiKeyField() {
        Bundle rawData = new Bundle();
        rawData.putBoolean(Constants.ServiceConfig.ENABLED_KEY, true);

        assertThat(parser.fromBundle(rawData)).usingRecursiveComparison().isEqualTo(new ServiceApphudConfig(
            true,
            Constants.Defaults.DEFAULT_API_KEY
        ));
    }

    @Test
    public void parseIfNoEnabledAndApiKeyField() {
        Bundle rawData = new Bundle();

        assertThat(parser.fromBundle(rawData)).usingRecursiveComparison().isEqualTo(new ServiceApphudConfig(
            false,
            Constants.Defaults.DEFAULT_API_KEY
        ));
    }
}
