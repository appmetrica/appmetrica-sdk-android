package io.appmetrica.analytics.apphud.internal;

import android.content.Context;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.ApphudActivator;
import io.appmetrica.analytics.apphud.impl.ClientModuleConfigStorage;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.config.client.ClientApphudConfig;
import io.appmetrica.analytics.apphud.impl.config.client.ClientApphudConfigChecker;
import io.appmetrica.analytics.apphud.impl.config.service.ServiceApphudConfig;
import io.appmetrica.analytics.apphud.impl.config.service.BundleToServiceApphudConfigConverter;
import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.ClientStorageProvider;
import io.appmetrica.analytics.modulesapi.internal.client.ModuleServiceConfig;
import io.appmetrica.analytics.testutils.CommonTest;
import io.appmetrica.analytics.testutils.MockedConstructionRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static io.appmetrica.analytics.assertions.AssertionsKt.ObjectPropertyAssertions;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class ApphudClientModuleEntryPointTest extends CommonTest {

    @NonNull
    private final String apiKey = "some_api_key";
    @NonNull
    private final String deviceId = "some_device_id";
    @NonNull
    private final String uuid = "some_uuid";

    @Mock
    private Context context;
    @Mock
    private ClientContext clientContext;
    @Mock
    private ClientStorageProvider clientStorageProvider;
    @Mock
    private ClientApphudConfig clientApphudConfig;
    @Mock
    private ServiceApphudConfig serviceApphudConfig;
    @Mock
    private SdkIdentifiers sdkIdentifiers;
    @Mock
    private ModuleServiceConfig<ServiceApphudConfig> moduleServiceConfig;

    @Rule
    public MockedConstructionRule<ClientModuleConfigStorage> storageRule =
        new MockedConstructionRule<>(ClientModuleConfigStorage.class);
    @Rule
    public MockedConstructionRule<BundleToServiceApphudConfigConverter> bundleParserRule =
        new MockedConstructionRule<>(BundleToServiceApphudConfigConverter.class);
    @Rule
    public MockedConstructionRule<ClientApphudConfigChecker> configCheckerRule =
        new MockedConstructionRule<>(ClientApphudConfigChecker.class);
    @Rule
    public MockedConstructionRule<ApphudActivator> apphudActivatorRule =
        new MockedConstructionRule<>(ApphudActivator.class);

    @Captor
    private ArgumentCaptor<ClientApphudConfig> clientModuleConfigCaptor;

    private ApphudClientModuleEntryPoint entryPoint;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        entryPoint = new ApphudClientModuleEntryPoint();
        when(clientContext.getContext()).thenReturn(context);
        when(clientContext.getClientStorageProvider()).thenReturn(clientStorageProvider);

        when(serviceApphudConfig.getApiKey()).thenReturn(apiKey);
        when(sdkIdentifiers.getDeviceId()).thenReturn(deviceId);
        when(sdkIdentifiers.getUuid()).thenReturn(uuid);
        when(moduleServiceConfig.getFeaturesConfig()).thenReturn(serviceApphudConfig);
        when(moduleServiceConfig.getIdentifiers()).thenReturn(sdkIdentifiers);
    }

    @Test
    public void getIdentifier() {
        assertThat(entryPoint.getIdentifier()).isEqualTo(Constants.MODULE_ID);
    }

    @Test
    public void getBundleParser() {
        assertThat(entryPoint.getServiceConfigExtensionConfiguration().getBundleConverter()).isSameAs(bundleParser());
    }

    @Test
    public void initClientSide() {
        entryPoint.initClientSide(clientContext);
        assertThat(storageRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(storageRule.getArgumentInterceptor().flatArguments()).containsExactly(clientStorageProvider);
    }

    @Test
    public void onActivatedIfContextIsNull() {
        when(clientContext.getContext()).thenReturn(null);
        entryPoint.initClientSide(clientContext);
        entryPoint.onActivated();
        verifyNoInteractions(apphudActivator(), storage());
    }

    @Test
    public void onActivatedIfInitClientSideWasNotCalled() {
        entryPoint.onActivated();
        verifyNoInteractions(apphudActivator());
    }

    @Test
    public void onActivated() {
        entryPoint.initClientSide(clientContext);

        when(storage().load()).thenReturn(clientApphudConfig);

        entryPoint.onActivated();

        verify(storage()).load();
        verify(apphudActivator()).activateIfNecessary(context, clientApphudConfig);
    }

    @Test
    public void onActivatedIfHasConfig() {
        entryPoint.initClientSide(clientContext);
        entryPoint.getServiceConfigExtensionConfiguration().getServiceConfigUpdateListener()
            .onServiceConfigUpdated(moduleServiceConfig);

        clearInvocations(storage(), apphudActivator());

        entryPoint.onActivated();

        verifyNoInteractions(storage());
        verify(apphudActivator()).activateIfNecessary(eq(context), any());
    }

    @Test
    public void configUpdateListener() {
        when(serviceApphudConfig.isEnabled()).thenReturn(true);

        entryPoint.initClientSide(clientContext);
        entryPoint.getServiceConfigExtensionConfiguration().getServiceConfigUpdateListener()
            .onServiceConfigUpdated(moduleServiceConfig);

        verify(apphudActivator()).activateIfNecessary(eq(context), clientModuleConfigCaptor.capture());
        ClientApphudConfig configForActivation = clientModuleConfigCaptor.getValue();
        verify(storage()).save(clientModuleConfigCaptor.capture());
        ClientApphudConfig configForStorage = clientModuleConfigCaptor.getValue();

        assertThat(configForStorage).isSameAs(configForActivation);
        ObjectPropertyAssertions(configForActivation)
            .withPrivateFields(true)
            .checkField("apiKey", apiKey)
            .checkField("deviceId", deviceId)
            .checkField("uuid", uuid)
            .checkAll();
    }

    @Test
    public void configUpdateListenerIfFeatureIsDisabled() {
        when(serviceApphudConfig.isEnabled()).thenReturn(false);

        entryPoint.initClientSide(clientContext);
        entryPoint.getServiceConfigExtensionConfiguration().getServiceConfigUpdateListener()
            .onServiceConfigUpdated(moduleServiceConfig);

        verify(apphudActivator()).activateIfNecessary(eq(context), clientModuleConfigCaptor.capture());
        ClientApphudConfig configForActivation = clientModuleConfigCaptor.getValue();
        verify(storage()).save(clientModuleConfigCaptor.capture());
        ClientApphudConfig configForStorage = clientModuleConfigCaptor.getValue();

        assertThat(configForStorage).isSameAs(configForActivation);
        ObjectPropertyAssertions(configForActivation)
            .withPrivateFields(true)
            .checkField("apiKey", null)
            .checkField("deviceId", null)
            .checkField("uuid", null)
            .checkAll();
    }

    private BundleToServiceApphudConfigConverter bundleParser() {
        assertThat(bundleParserRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(bundleParserRule.getArgumentInterceptor().flatArguments()).isEmpty();
        return bundleParserRule.getConstructionMock().constructed().get(0);
    }

    private ClientApphudConfigChecker configChecker() {
        assertThat(configCheckerRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(configCheckerRule.getArgumentInterceptor().flatArguments()).isEmpty();
        return configCheckerRule.getConstructionMock().constructed().get(0);
    }

    private ApphudActivator apphudActivator() {
        assertThat(apphudActivatorRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(apphudActivatorRule.getArgumentInterceptor().flatArguments()).containsExactly(configChecker());
        return apphudActivatorRule.getConstructionMock().constructed().get(0);
    }

    private ClientModuleConfigStorage storage() {
        assertThat(storageRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(storageRule.getArgumentInterceptor().flatArguments()).containsExactly(clientStorageProvider);
        return storageRule.getConstructionMock().constructed().get(0);
    }
}
