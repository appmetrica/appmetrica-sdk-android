package io.appmetrica.analytics.apphud.impl;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.config.client.ClientApphudConfig;
import io.appmetrica.analytics.apphud.impl.config.client.ClientApphudConfigChecker;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedStaticRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;

public class ApphudActivatorTest extends CommonTest {

    @NonNull
    private final String apiKey = "some_api_key";
    @NonNull
    private final String deviceId = "some_device_id";
    @NonNull
    private final String uuid = "some_uuid";

    @Mock
    private Context context;
    @Mock
    private ClientApphudConfigChecker checker;
    @Mock
    private ClientApphudConfig config;

    @Rule
    public MockedStaticRule<ApphudWrapper> apphudWrapperRule = new MockedStaticRule<>(ApphudWrapper.class);

    private ApphudActivator activator;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        when(checker.doesNeedUpdate(config)).thenReturn(false);
        when(config.getApiKey()).thenReturn(apiKey);
        when(config.getDeviceId()).thenReturn(deviceId);
        when(config.getUuid()).thenReturn(uuid);

        activator = new ApphudActivator(checker);
    }

    @Test
    public void activateIfNecessary() {
        activator.activateIfNecessary(context, config);

        apphudWrapperRule.getStaticMock().verify(
            () -> ApphudWrapper.start(context, apiKey, uuid, deviceId, true)
        );
    }

    @Test
    public void doubleActivateIfNecessary() {
        activator.activateIfNecessary(context, config);
        activator.activateIfNecessary(context, config);

        apphudWrapperRule.getStaticMock().verify(
            () -> ApphudWrapper.start(context, apiKey, uuid, deviceId, true),
            times(1)
        );
    }

    @Test
    public void activateIfNecessaryIfConfigNeedsUpdate() {
        when(checker.doesNeedUpdate(config)).thenReturn(true);
        activator.activateIfNecessary(context, config);

        apphudWrapperRule.getStaticMock().verify(
            () -> ApphudWrapper.start(context, apiKey, uuid, deviceId, true),
            never()
        );
    }
}
