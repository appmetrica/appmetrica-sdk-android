package io.appmetrica.analytics.apphud.internal;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import io.appmetrica.analytics.apphud.impl.ApphudActivationConfigChecker;
import io.appmetrica.analytics.apphud.impl.ApphudActivationConfigStorage;
import io.appmetrica.analytics.apphud.impl.ApphudActivator;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.config.client.BundleToClientSideApphudConfigConverter;
import io.appmetrica.analytics.apphud.impl.config.client.model.ApphudActivationConfig;
import io.appmetrica.analytics.apphud.impl.config.client.model.ClientSideApphudConfig;
import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers;
import io.appmetrica.analytics.modulesapi.internal.client.BundleToServiceConfigConverter;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.ClientStorageProvider;
import io.appmetrica.analytics.modulesapi.internal.client.ModuleServiceConfig;
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigExtensionConfiguration;
import io.appmetrica.gradle.testutils.CommonTest;
import io.appmetrica.gradle.testutils.assertions.Assertions;
import io.appmetrica.gradle.testutils.rules.MockedConstructionRule;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

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
    private ApphudActivationConfig apphudActivationConfig;
    @Mock
    private ClientSideApphudConfig clientSideApphudConfig;
    @Mock
    private SdkIdentifiers sdkIdentifiers;
    @Mock
    private ModuleServiceConfig<ClientSideApphudConfigWrapper> moduleServiceConfig;

    private ClientSideApphudConfigWrapper wrapper;

    @Rule
    public MockedConstructionRule<ApphudActivationConfigStorage> storageRule =
        new MockedConstructionRule<>(ApphudActivationConfigStorage.class);
    @Rule
    public MockedConstructionRule<BundleToClientSideApphudConfigConverter> bundleConverterRule =
        new MockedConstructionRule<>(BundleToClientSideApphudConfigConverter.class);
    @Rule
    public MockedConstructionRule<ApphudActivationConfigChecker> configCheckerRule =
        new MockedConstructionRule<>(ApphudActivationConfigChecker.class);
    @Rule
    public MockedConstructionRule<ApphudActivator> apphudActivatorRule =
        new MockedConstructionRule<>(ApphudActivator.class);

    @Captor
    private ArgumentCaptor<ApphudActivationConfig> activationConfigCaptor;

    private ApphudClientModuleEntryPoint entryPoint;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        entryPoint = new ApphudClientModuleEntryPoint();
        when(clientContext.getContext()).thenReturn(context);
        when(clientContext.getClientStorageProvider()).thenReturn(clientStorageProvider);

        when(clientSideApphudConfig.getApiKey()).thenReturn(apiKey);
        when(sdkIdentifiers.getDeviceId()).thenReturn(deviceId);
        when(sdkIdentifiers.getUuid()).thenReturn(uuid);
        wrapper = ClientSideApphudConfigWrapper.toWrapper(clientSideApphudConfig);
        when(moduleServiceConfig.getFeaturesConfig()).thenReturn(wrapper);
        when(moduleServiceConfig.getIdentifiers()).thenReturn(sdkIdentifiers);
    }

    @Test
    public void getIdentifier() {
        assertThat(entryPoint.getIdentifier()).isEqualTo(Constants.MODULE_ID);
    }

    @Test
    public void bundleConverterDelegatesToInnerConverter() {
        Bundle bundle = new Bundle();
        when(bundleConverter().fromBundle(bundle)).thenReturn(clientSideApphudConfig);

        BundleToServiceConfigConverter<ClientSideApphudConfigWrapper> converter =
            entryPoint.getServiceConfigExtensionConfiguration().getBundleConverter();
        ClientSideApphudConfigWrapper result = converter.fromBundle(bundle);

        verify(bundleConverter()).fromBundle(bundle);
        assertThat(result.config).isSameAs(clientSideApphudConfig);
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

        when(storage().load()).thenReturn(apphudActivationConfig);

        entryPoint.onActivated();

        verify(storage()).load();
        verify(apphudActivator()).activateIfNecessary(context, apphudActivationConfig);
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
        when(clientSideApphudConfig.isEnabled()).thenReturn(true);

        entryPoint.initClientSide(clientContext);
        entryPoint.getServiceConfigExtensionConfiguration().getServiceConfigUpdateListener()
            .onServiceConfigUpdated(moduleServiceConfig);

        verify(apphudActivator()).activateIfNecessary(eq(context), activationConfigCaptor.capture());
        ApphudActivationConfig configForActivation = activationConfigCaptor.getValue();
        verify(storage()).save(activationConfigCaptor.capture());
        ApphudActivationConfig configForStorage = activationConfigCaptor.getValue();

        assertThat(configForStorage).isSameAs(configForActivation);
        Assertions.INSTANCE.ObjectPropertyAssertions(configForActivation)
            .withPrivateFields(true)
            .checkField("apiKey", apiKey)
            .checkField("deviceId", deviceId)
            .checkField("uuid", uuid)
            .checkAll();
    }

    @Test
    public void configUpdateListenerIfFeatureIsDisabled() {
        when(clientSideApphudConfig.isEnabled()).thenReturn(false);

        entryPoint.initClientSide(clientContext);
        entryPoint.getServiceConfigExtensionConfiguration().getServiceConfigUpdateListener()
            .onServiceConfigUpdated(moduleServiceConfig);

        verify(apphudActivator()).activateIfNecessary(eq(context), activationConfigCaptor.capture());
        ApphudActivationConfig configForActivation = activationConfigCaptor.getValue();
        verify(storage()).save(activationConfigCaptor.capture());
        ApphudActivationConfig configForStorage = activationConfigCaptor.getValue();

        assertThat(configForStorage).isSameAs(configForActivation);
        Assertions.INSTANCE.ObjectPropertyAssertions(configForActivation)
            .withPrivateFields(true)
            .checkField("apiKey", null)
            .checkField("deviceId", null)
            .checkField("uuid", null)
            .checkAll();
    }

    @Test
    public void configUpdateListenerWithNullWrapper() {
        when(moduleServiceConfig.getFeaturesConfig()).thenReturn(null);

        entryPoint.initClientSide(clientContext);
        entryPoint.getServiceConfigExtensionConfiguration().getServiceConfigUpdateListener()
            .onServiceConfigUpdated(moduleServiceConfig);

        verifyNoInteractions(apphudActivator(), storage());
    }

    @Test
    public void serviceConfigExtensionConfigurationIsCached() {
        ServiceConfigExtensionConfiguration<ClientSideApphudConfigWrapper> first =
            entryPoint.getServiceConfigExtensionConfiguration();
        ServiceConfigExtensionConfiguration<ClientSideApphudConfigWrapper> second =
            entryPoint.getServiceConfigExtensionConfiguration();

        assertThat(first).isSameAs(second);
    }

    @Test
    public void bundleConverterIsCached() {
        ServiceConfigExtensionConfiguration<ClientSideApphudConfigWrapper> ext =
            entryPoint.getServiceConfigExtensionConfiguration();
        assertThat(ext.getBundleConverter()).isSameAs(ext.getBundleConverter());
    }

    @Test
    public void serviceConfigUpdateListenerIsCached() {
        ServiceConfigExtensionConfiguration<ClientSideApphudConfigWrapper> ext =
            entryPoint.getServiceConfigExtensionConfiguration();
        assertThat(ext.getServiceConfigUpdateListener()).isSameAs(ext.getServiceConfigUpdateListener());
    }

    private BundleToClientSideApphudConfigConverter bundleConverter() {
        assertThat(bundleConverterRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(bundleConverterRule.getArgumentInterceptor().flatArguments()).isEmpty();
        return bundleConverterRule.getConstructionMock().constructed().get(0);
    }

    private ApphudActivationConfigChecker configChecker() {
        assertThat(configCheckerRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(configCheckerRule.getArgumentInterceptor().flatArguments()).isEmpty();
        return configCheckerRule.getConstructionMock().constructed().get(0);
    }

    private ApphudActivator apphudActivator() {
        assertThat(apphudActivatorRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(apphudActivatorRule.getArgumentInterceptor().flatArguments()).containsExactly(configChecker());
        return apphudActivatorRule.getConstructionMock().constructed().get(0);
    }

    private ApphudActivationConfigStorage storage() {
        assertThat(storageRule.getConstructionMock().constructed()).hasSize(1);
        assertThat(storageRule.getArgumentInterceptor().flatArguments()).containsExactly(clientStorageProvider);
        return storageRule.getConstructionMock().constructed().get(0);
    }
}
