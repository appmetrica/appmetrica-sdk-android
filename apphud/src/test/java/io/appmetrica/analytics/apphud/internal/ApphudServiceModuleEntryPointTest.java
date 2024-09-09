package io.appmetrica.analytics.apphud.internal;

import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.config.remote.RemoteApphudConfig;
import io.appmetrica.analytics.apphud.impl.config.remote.RemoteApphudConfigBundleConverter;
import io.appmetrica.analytics.apphud.impl.config.remote.RemoteApphudConfigConverter;
import io.appmetrica.analytics.apphud.impl.config.remote.RemoteApphudConfigParser;
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig;
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import java.util.Map;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class ApphudServiceModuleEntryPointTest extends CommonTest {

    @Mock
    private RemoteApphudConfig remoteApphudConfig;
    @Mock
    private ServiceContext serviceContext;
    @Mock
    private ModuleRemoteConfig<RemoteApphudConfig> initialConfig;

    @Rule
    public MockedConstructionRule<RemoteApphudConfigBundleConverter> bundleConverterRule =
        new MockedConstructionRule<>(RemoteApphudConfigBundleConverter.class);
    @Rule
    public MockedConstructionRule<RemoteApphudConfigConverter> configConverterRule =
        new MockedConstructionRule<>(RemoteApphudConfigConverter.class);
    @Rule
    public MockedConstructionRule<RemoteApphudConfigParser> configParserRule =
        new MockedConstructionRule<>(RemoteApphudConfigParser.class);

    private ApphudServiceModuleEntryPoint entryPoint;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        entryPoint = new ApphudServiceModuleEntryPoint();
    }

    @Test
    public void configToBundleBeforeInit() {
        entryPoint.getClientConfigProvider().getConfigBundleForClient();

        verify(bundleConverter()).convert(null);
    }

    @Test
    public void configToBundleAfterInit() {
        when(initialConfig.getFeaturesConfig()).thenReturn(remoteApphudConfig);
        entryPoint.initServiceSide(
            serviceContext,
            initialConfig
        );
        entryPoint.getClientConfigProvider().getConfigBundleForClient();

        verify(bundleConverter()).convert(remoteApphudConfig);
    }

    @Test
    public void getIdentifier() {
        assertThat(entryPoint.getIdentifier()).isEqualTo(Constants.MODULE_ID);
    }

    @Test
    public void getFeatures() {
        assertThat(entryPoint.getRemoteConfigExtensionConfiguration().getFeatures()).containsExactly(
            Constants.RemoteConfig.FEATURE_NAME_OBFUSCATED
        );
    }

    @Test
    public void getBlocks() {
        assertThat(entryPoint.getRemoteConfigExtensionConfiguration().getBlocks()).containsExactlyEntriesOf(
            Map.of(
                Constants.RemoteConfig.BLOCK_NAME_OBFUSCATED, Constants.RemoteConfig.BLOCK_VERSION
            )
        );
    }

    @Test
    public void getJsonParser() {
        assertThat(entryPoint.getRemoteConfigExtensionConfiguration().getJsonParser()).isSameAs(
            configParser()
        );
    }

    @Test
    public void getProtobufConverter() {
        assertThat(entryPoint.getRemoteConfigExtensionConfiguration().getProtobufConverter()).isSameAs(
            configConverter()
        );
    }

    @Test
    public void configUpdateListener() {
        when(initialConfig.getFeaturesConfig()).thenReturn(remoteApphudConfig);
        entryPoint.getRemoteConfigExtensionConfiguration().getRemoteConfigUpdateListener()
            .onRemoteConfigUpdated(initialConfig);
        entryPoint.getClientConfigProvider().getConfigBundleForClient();

        verify(bundleConverter()).convert(remoteApphudConfig);
    }

    private RemoteApphudConfigBundleConverter bundleConverter() {
        assertThat(bundleConverterRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(bundleConverterRule.getArgumentInterceptor().flatArguments()).isEmpty();
        return bundleConverterRule.getConstructionMock().constructed().get(0);
    }

    private RemoteApphudConfigParser configParser() {
        assertThat(configParserRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(configParserRule.getArgumentInterceptor().flatArguments()).isEmpty();
        return configParserRule.getConstructionMock().constructed().get(0);
    }

    private RemoteApphudConfigConverter configConverter() {
        assertThat(configConverterRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(configConverterRule.getArgumentInterceptor().flatArguments()).isEmpty();
        return configConverterRule.getConstructionMock().constructed().get(0);
    }
}
