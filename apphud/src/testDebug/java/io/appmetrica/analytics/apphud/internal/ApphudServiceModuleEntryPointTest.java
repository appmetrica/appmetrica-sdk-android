package io.appmetrica.analytics.apphud.internal;

import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.config.service.ServiceSideApphudConfigConverter;
import io.appmetrica.analytics.apphud.impl.config.service.ServiceSideApphudConfigParser;
import io.appmetrica.analytics.apphud.impl.config.service.ServiceSideApphudConfigToBundleConverter;
import io.appmetrica.analytics.apphud.impl.config.service.model.ServiceSideApphudConfig;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.coreapi.internal.data.JsonParser;
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig;
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigExtensionConfiguration;
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigUpdateListener;
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext;
import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule;
import java.util.Map;
import org.json.JSONObject;
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
    private ServiceSideApphudConfig serviceSideApphudConfig;
    @Mock
    private ServiceContext serviceContext;
    @Mock
    private ModuleRemoteConfig<ServiceSideApphudConfigWrapper> initialConfig;

    @Rule
    public MockedConstructionRule<ServiceSideApphudConfigToBundleConverter> bundleConverterRule =
        new MockedConstructionRule<>(ServiceSideApphudConfigToBundleConverter.class);
    @Rule
    public MockedConstructionRule<ServiceSideApphudConfigConverter> configConverterRule =
        new MockedConstructionRule<>(ServiceSideApphudConfigConverter.class);
    @Rule
    public MockedConstructionRule<ServiceSideApphudConfigParser> configParserRule =
        new MockedConstructionRule<>(ServiceSideApphudConfigParser.class);

    private ApphudServiceModuleEntryPoint entryPoint;
    private ServiceSideApphudConfigWrapper wrapper;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        entryPoint = new ApphudServiceModuleEntryPoint();
        wrapper = ServiceSideApphudConfigWrapper.toWrapper(serviceSideApphudConfig);
    }

    @Test
    public void configToBundleBeforeInit() {
        entryPoint.getClientConfigProvider().getConfigBundleForClient();

        verify(bundleConverter()).convert(null);
    }

    @Test
    public void configToBundleAfterInit() {
        when(initialConfig.getFeaturesConfig()).thenReturn(wrapper);
        entryPoint.initServiceSide(
            serviceContext,
            initialConfig
        );
        entryPoint.getClientConfigProvider().getConfigBundleForClient();

        verify(bundleConverter()).convert(serviceSideApphudConfig);
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
    public void jsonParserDelegatesToConfigParser() {
        JSONObject rawData = new JSONObject();
        when(configParser().parse(rawData)).thenReturn(serviceSideApphudConfig);

        JsonParser<ServiceSideApphudConfigWrapper> parser =
            entryPoint.getRemoteConfigExtensionConfiguration().getJsonParser();
        ServiceSideApphudConfigWrapper result = parser.parse(rawData);

        verify(configParser()).parse(rawData);
        assertThat(result.config).isSameAs(serviceSideApphudConfig);
    }

    @Test
    public void jsonParserParseOrNullDelegatesToConfigParser() {
        JSONObject rawData = new JSONObject();
        when(configParser().parseOrNull(rawData)).thenReturn(serviceSideApphudConfig);

        JsonParser<ServiceSideApphudConfigWrapper> parser =
            entryPoint.getRemoteConfigExtensionConfiguration().getJsonParser();
        ServiceSideApphudConfigWrapper result = parser.parseOrNull(rawData);

        verify(configParser()).parseOrNull(rawData);
        assertThat(result.config).isSameAs(serviceSideApphudConfig);
    }

    @Test
    public void jsonParserParseOrNullReturnsNullWhenConfigParserReturnsNull() {
        JSONObject rawData = new JSONObject();
        when(configParser().parseOrNull(rawData)).thenReturn(null);

        JsonParser<ServiceSideApphudConfigWrapper> parser =
            entryPoint.getRemoteConfigExtensionConfiguration().getJsonParser();

        assertThat(parser.parseOrNull(rawData)).isNull();
    }

    @Test
    public void protobufConverterDelegatesToConfigConverter() {
        byte[] bytes = new byte[] {1, 2, 3};
        when(configConverter().fromModel(serviceSideApphudConfig)).thenReturn(bytes);
        when(configConverter().toModel(bytes)).thenReturn(serviceSideApphudConfig);

        Converter<ServiceSideApphudConfigWrapper, byte[]> converter =
            entryPoint.getRemoteConfigExtensionConfiguration().getProtobufConverter();

        assertThat(converter.fromModel(wrapper)).isSameAs(bytes);
        verify(configConverter()).fromModel(serviceSideApphudConfig);

        ServiceSideApphudConfigWrapper restored = converter.toModel(bytes);
        verify(configConverter()).toModel(bytes);
        assertThat(restored.config).isSameAs(serviceSideApphudConfig);
    }

    @Test
    public void configUpdateListener() {
        when(initialConfig.getFeaturesConfig()).thenReturn(wrapper);
        entryPoint.getRemoteConfigExtensionConfiguration().getRemoteConfigUpdateListener()
            .onRemoteConfigUpdated(initialConfig);
        entryPoint.getClientConfigProvider().getConfigBundleForClient();

        verify(bundleConverter()).convert(serviceSideApphudConfig);
    }

    @Test
    public void configUpdateListenerWithNullFeaturesConfig() {
        when(initialConfig.getFeaturesConfig()).thenReturn(null);
        entryPoint.getRemoteConfigExtensionConfiguration().getRemoteConfigUpdateListener()
            .onRemoteConfigUpdated(initialConfig);
        entryPoint.getClientConfigProvider().getConfigBundleForClient();

        verify(bundleConverter()).convert(null);
    }

    @Test
    public void remoteConfigExtensionConfigurationIsCached() {
        RemoteConfigExtensionConfiguration<ServiceSideApphudConfigWrapper> first =
            entryPoint.getRemoteConfigExtensionConfiguration();
        RemoteConfigExtensionConfiguration<ServiceSideApphudConfigWrapper> second =
            entryPoint.getRemoteConfigExtensionConfiguration();

        assertThat(first).isSameAs(second);
    }

    @Test
    public void clientConfigProviderIsCached() {
        assertThat(entryPoint.getClientConfigProvider()).isSameAs(entryPoint.getClientConfigProvider());
    }

    @Test
    public void jsonParserIsCached() {
        RemoteConfigExtensionConfiguration<ServiceSideApphudConfigWrapper> ext =
            entryPoint.getRemoteConfigExtensionConfiguration();
        assertThat(ext.getJsonParser()).isSameAs(ext.getJsonParser());
    }

    @Test
    public void protobufConverterIsCached() {
        RemoteConfigExtensionConfiguration<ServiceSideApphudConfigWrapper> ext =
            entryPoint.getRemoteConfigExtensionConfiguration();
        assertThat(ext.getProtobufConverter()).isSameAs(ext.getProtobufConverter());
    }

    @Test
    public void remoteConfigUpdateListenerIsCached() {
        RemoteConfigExtensionConfiguration<ServiceSideApphudConfigWrapper> ext =
            entryPoint.getRemoteConfigExtensionConfiguration();
        RemoteConfigUpdateListener<ServiceSideApphudConfigWrapper> first = ext.getRemoteConfigUpdateListener();
        RemoteConfigUpdateListener<ServiceSideApphudConfigWrapper> second = ext.getRemoteConfigUpdateListener();
        assertThat(first).isSameAs(second);
    }

    private ServiceSideApphudConfigToBundleConverter bundleConverter() {
        assertThat(bundleConverterRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(bundleConverterRule.getArgumentInterceptor().flatArguments()).isEmpty();
        return bundleConverterRule.getConstructionMock().constructed().get(0);
    }

    private ServiceSideApphudConfigParser configParser() {
        assertThat(configParserRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(configParserRule.getArgumentInterceptor().flatArguments()).isEmpty();
        return configParserRule.getConstructionMock().constructed().get(0);
    }

    private ServiceSideApphudConfigConverter configConverter() {
        assertThat(configConverterRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(configConverterRule.getArgumentInterceptor().flatArguments()).isEmpty();
        return configConverterRule.getConstructionMock().constructed().get(0);
    }
}
