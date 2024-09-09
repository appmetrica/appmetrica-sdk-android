package io.appmetrica.analytics.apphud.internal;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.apphud.impl.ApphudActivator;
import io.appmetrica.analytics.apphud.impl.ClientModuleConfigStorage;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.config.client.ClientApphudConfig;
import io.appmetrica.analytics.apphud.impl.config.client.ClientApphudConfigChecker;
import io.appmetrica.analytics.apphud.impl.config.service.ServiceApphudConfig;
import io.appmetrica.analytics.apphud.impl.config.service.BundleToServiceApphudConfigConverter;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.client.BundleToServiceConfigConverter;
import io.appmetrica.analytics.modulesapi.internal.client.ClientContext;
import io.appmetrica.analytics.modulesapi.internal.client.ModuleClientEntryPoint;
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigExtensionConfiguration;
import io.appmetrica.analytics.modulesapi.internal.client.ServiceConfigUpdateListener;

public class ApphudClientModuleEntryPoint extends ModuleClientEntryPoint<ServiceApphudConfig> {

    private static final String TAG = "[ApphudClientModuleEntryPoint]";

    @Nullable
    private Context context;
    @Nullable
    private ClientModuleConfigStorage storage = null;
    @Nullable
    private ClientApphudConfig config = null;

    @NonNull
    private final BundleToServiceApphudConfigConverter bundleParser = new BundleToServiceApphudConfigConverter();
    @NonNull
    private final ClientApphudConfigChecker configChecker = new ClientApphudConfigChecker();
    @NonNull
    private final ApphudActivator apphudActivator = new ApphudActivator(configChecker);
    @NonNull
    private final ServiceConfigUpdateListener<ServiceApphudConfig> configUpdateListener = config -> {
        synchronized(ApphudClientModuleEntryPoint.this) {
            DebugLogger.INSTANCE.info(TAG, "received config " + config);
            ClientApphudConfig newConfig = new ClientApphudConfig(null, null, null);
            if (config.getFeaturesConfig().isEnabled()) {
                newConfig = new ClientApphudConfig(
                    config.getFeaturesConfig().getApiKey(),
                    config.getIdentifiers().getDeviceId(),
                    config.getIdentifiers().getUuid()
                );
            }
            ApphudClientModuleEntryPoint.this.config = newConfig;
            if (context != null) {
                DebugLogger.INSTANCE.info(TAG, "Activate Apphud from listener " + newConfig);
                apphudActivator.activateIfNecessary(context, newConfig);
            }
            if (storage != null) {
                storage.save(newConfig);
            }
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
        storage = new ClientModuleConfigStorage(clientContext.getClientStorageProvider());
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
    public ServiceConfigExtensionConfiguration<ServiceApphudConfig> getServiceConfigExtensionConfiguration() {
        return new ServiceConfigExtensionConfiguration<ServiceApphudConfig>() {
            @NonNull
            @Override
            public BundleToServiceConfigConverter<ServiceApphudConfig> getBundleConverter() {
                return bundleParser;
            }

            @NonNull
            @Override
            public ServiceConfigUpdateListener<ServiceApphudConfig> getServiceConfigUpdateListener() {
                return configUpdateListener;
            }
        };
    }
}
