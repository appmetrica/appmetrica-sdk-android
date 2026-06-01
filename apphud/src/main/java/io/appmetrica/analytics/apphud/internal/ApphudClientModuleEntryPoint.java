package io.appmetrica.analytics.apphud.internal;

import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.apphud.impl.ApphudActivationConfigChecker;
import io.appmetrica.analytics.apphud.impl.ApphudActivationConfigStorage;
import io.appmetrica.analytics.apphud.impl.ApphudActivator;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.config.client.BundleToClientSideApphudConfigConverter;
import io.appmetrica.analytics.apphud.impl.config.client.model.ApphudActivationConfig;
import io.appmetrica.analytics.apphud.impl.config.client.model.ClientSideApphudConfig;
import io.appmetrica.analytics.coreapi.internal.identifiers.SdkIdentifiers;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.BundleToServiceConfigConverter;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint;
import io.appmetrica.analytics.modulesapi.internal.client.ModuleServiceConfig;
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigExtensionConfiguration;
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigUpdateListener;

public class ApphudClientModuleEntryPoint extends ModuleClientEntryPoint<ClientSideApphudConfigWrapper> {

    private static final String TAG = "[ApphudClientModuleEntryPoint]";

    @Nullable
    private Context context;
    @Nullable
    private ApphudActivationConfigStorage storage = null;
    @Nullable
    private ApphudActivationConfig config = null;

    @NonNull
    private final BundleToClientSideApphudConfigConverter bundleConverter =
        new BundleToClientSideApphudConfigConverter();
    @NonNull
    private final ApphudActivationConfigChecker configChecker = new ApphudActivationConfigChecker();
    @NonNull
    private final ApphudActivator apphudActivator = new ApphudActivator(configChecker);

    @NonNull
    private final BundleToServiceConfigConverter<ClientSideApphudConfigWrapper> wrapperBundleConverter =
        new BundleToServiceConfigConverter<ClientSideApphudConfigWrapper>() {
            @NonNull
            @Override
            public ClientSideApphudConfigWrapper fromBundle(@NonNull Bundle bundle) {
                return ClientSideApphudConfigWrapper.toWrapper(bundleConverter.fromBundle(bundle));
            }
        };

    @NonNull
    private final ServiceConfigUpdateListener<ClientSideApphudConfigWrapper> configUpdateListener =
        new ServiceConfigUpdateListener<ClientSideApphudConfigWrapper>() {
            @Override
            public void onServiceConfigUpdated(
                @NonNull ModuleServiceConfig<ClientSideApphudConfigWrapper> moduleConfig
            ) {
                synchronized(ApphudClientModuleEntryPoint.this) {
                    DebugLogger.INSTANCE.info(TAG, "received config " + moduleConfig);
                    ClientSideApphudConfigWrapper wrapper = moduleConfig.getFeaturesConfig();
                    if (wrapper == null) {
                        return;
                    }
                    ClientSideApphudConfig clientSideConfig = wrapper.config;
                    SdkIdentifiers identifiers = moduleConfig.getIdentifiers();
                    ApphudActivationConfig newConfig = clientSideConfig.isEnabled()
                        ? new ApphudActivationConfig(
                            clientSideConfig.getApiKey(),
                            identifiers.getDeviceId(),
                            identifiers.getUuid()
                        )
                        : new ApphudActivationConfig(null, null, null);
                    ApphudClientModuleEntryPoint.this.config = newConfig;
                    if (context != null) {
                        DebugLogger.INSTANCE.info(TAG, "Activate Apphud from listener " + newConfig);
                        apphudActivator.activateIfNecessary(context, newConfig);
                    }
                    if (storage != null) {
                        storage.save(newConfig);
                    }
                }
            }
        };

    @NonNull
    private final ServiceConfigExtensionConfiguration<ClientSideApphudConfigWrapper> serviceExtensionConfiguration =
        new ServiceConfigExtensionConfiguration<ClientSideApphudConfigWrapper>() {
            @NonNull
            @Override
            public BundleToServiceConfigConverter<ClientSideApphudConfigWrapper> getBundleConverter() {
                return wrapperBundleConverter;
            }

            @NonNull
            @Override
            public ServiceConfigUpdateListener<ClientSideApphudConfigWrapper> getServiceConfigUpdateListener() {
                return configUpdateListener;
            }
        };

    @NonNull
    @Override
    public String getIdentifier() {
        return Constants.MODULE_ID;
    }

    @Override
    public void initClientSide(@NonNull ClientContext clientContext) {
        context = clientContext.getContext();
        storage = new ApphudActivationConfigStorage(clientContext.getClientStorageProvider());
    }

    @Override
    public void onActivated() {
        if (context != null && storage != null) {
            DebugLogger.INSTANCE.info(TAG, "Activate Apphud");
            if (config == null) {
                config = storage.load();
            }
            apphudActivator.activateIfNecessary(context, config);
        }
    }

    @Nullable
    @Override
    public ServiceConfigExtensionConfiguration<ClientSideApphudConfigWrapper> getServiceConfigExtensionConfiguration() {
        return serviceExtensionConfiguration;
    }
}
