package io.appmetrica.analytics.apphud.impl.config.service;

import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.config.service.model.ServiceSideApphudConfig;
import io.appmetrica.analytics.coreutils.internal.parsing.RemoteConfigJsonUtils;
import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.gradle.testutils.rules.MockedStaticRule;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Rule;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

public class ServiceSideApphudConfigParserTest extends CommonTest {

    @Rule
    public MockedStaticRule<RemoteConfigJsonUtils> remoteConfigJsonUtilsRule =
        new MockedStaticRule<>(RemoteConfigJsonUtils.class);

    @NonNull
    private final ServiceSideApphudConfigParser parser = new ServiceSideApphudConfigParser();

    @Test
    public void parse() throws JSONException {
        String apiKey = "some_api_key";
        JSONObject rawData = new JSONObject();
        rawData.put(Constants.RemoteConfig.BLOCK_NAME, new JSONObject()
            .put(Constants.RemoteConfig.API_KEY_KEY, apiKey)
        );
        when(RemoteConfigJsonUtils.extractFeature(
            rawData,
            Constants.RemoteConfig.FEATURE_NAME,
            Constants.Defaults.DEFAULT_FEATURE_STATE
        )).thenReturn(true);

        assertThat(parser.parse(rawData)).usingRecursiveComparison().isEqualTo(new ServiceSideApphudConfig(
            true,
            apiKey
        ));
    }

    @Test
    public void parseIfFeatureDisabled() throws JSONException {
        String apiKey = "some_api_key";
        JSONObject rawData = new JSONObject();
        rawData.put(Constants.RemoteConfig.BLOCK_NAME, new JSONObject()
            .put(Constants.RemoteConfig.API_KEY_KEY, apiKey)
        );
        when(RemoteConfigJsonUtils.extractFeature(
            rawData,
            Constants.RemoteConfig.FEATURE_NAME,
            Constants.Defaults.DEFAULT_FEATURE_STATE
        )).thenReturn(false);

        assertThat(parser.parse(rawData)).usingRecursiveComparison().isEqualTo(new ServiceSideApphudConfig(
            false,
            apiKey
        ));
    }

    @Test
    public void parseIfNoApiKey() throws JSONException {
        JSONObject rawData = new JSONObject();
        rawData.put(Constants.RemoteConfig.BLOCK_NAME, new JSONObject());
        when(RemoteConfigJsonUtils.extractFeature(
            rawData,
            Constants.RemoteConfig.FEATURE_NAME,
            Constants.Defaults.DEFAULT_FEATURE_STATE
        )).thenReturn(true);

        assertThat(parser.parse(rawData)).usingRecursiveComparison().isEqualTo(new ServiceSideApphudConfig(
            true,
            Constants.Defaults.DEFAULT_API_KEY
        ));
    }

    @Test
    public void parseIfNoModuleBlock() {
        JSONObject rawData = new JSONObject();
        when(RemoteConfigJsonUtils.extractFeature(
            rawData,
            Constants.RemoteConfig.FEATURE_NAME,
            Constants.Defaults.DEFAULT_FEATURE_STATE
        )).thenReturn(true);

        assertThat(parser.parse(rawData)).usingRecursiveComparison().isEqualTo(new ServiceSideApphudConfig(
            true,
            Constants.Defaults.DEFAULT_API_KEY
        ));
    }
}
