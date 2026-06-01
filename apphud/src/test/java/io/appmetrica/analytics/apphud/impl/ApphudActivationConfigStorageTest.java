package io.appmetrica.analytics.apphud.impl;

import io.appmetrica.analytics.apphud.impl.config.client.model.ApphudActivationConfig;
import io.appmetrica.analytics.modulesapi.internal.client.ClientStorageProvider;
import io.appmetrica.analytics.modulesapi.internal.common.ModulePreferences;
import io.appmetrica.gradle.testutils.assertions.Assertions;
import io.appmetrica.gradle.testutils.CommonTest;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApphudActivationConfigStorageTest extends CommonTest {

    @Mock
    private ClientStorageProvider storageProvider;
    @Mock
    private ModulePreferences preferences;

    private ApphudActivationConfigStorage storage;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(storageProvider.modulePreferences(Constants.MODULE_ID)).thenReturn(preferences);

        storage = new ApphudActivationConfigStorage(storageProvider);
    }

    @Test
    public void load() {
        String apiKey = "some_api_key";
        String deviceId = "some_device_id";
        String uuid = "some_uuid";
        when(preferences.getString(Constants.ClientConfig.API_KEY_KEY, null)).thenReturn(apiKey);
        when(preferences.getString(Constants.ClientConfig.DEVICE_ID_KEY, null)).thenReturn(deviceId);
        when(preferences.getString(Constants.ClientConfig.UUID_KEY, null)).thenReturn(uuid);

        ApphudActivationConfig config = storage.load();
        Assertions.INSTANCE.ObjectPropertyAssertions(config)
            .withPrivateFields(true)
            .checkField("apiKey", apiKey)
            .checkField("deviceId", deviceId)
            .checkField("uuid", uuid)
            .checkAll();
    }

    @Test
    public void save() {
        String apiKey = "some_api_key";
        String deviceId = "some_device_id";
        String uuid = "some_uuid";
        ApphudActivationConfig config = new ApphudActivationConfig(
            apiKey,
            deviceId,
            uuid
        );

        storage.save(config);

        verify(preferences).putString(Constants.ClientConfig.API_KEY_KEY, apiKey);
        verify(preferences).putString(Constants.ClientConfig.DEVICE_ID_KEY, deviceId);
        verify(preferences).putString(Constants.ClientConfig.UUID_KEY, uuid);
    }
}
