package io.appmetrica.analytics.apphud.internal;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.config.remote.RemoteApphudConfig;
import io.appmetrica.analytics.apphud.impl.config.remote.RemoteApphudConfigBundleConverter;
import io.appmetrica.analytics.apphud.impl.config.remote.RemoteApphudConfigConverter;
import io.appmetrica.analytics.apphud.impl.config.remote.RemoteApphudConfigParser;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.coreapi.internal.data.JsonParser;
import io.appmetrica.analytics.logger.appmetrica.internal.DebugLogger;
import io.appmetrica.analytics.modulesapi.internal.service.ClientConfigProvider;
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig;
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint;
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigExtensionConfiguration;
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigUpdateListener;
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ApphudServiceModuleEntryPoint extends ModuleServiceEntryPoint<RemoteApphudConfig> {

    private static final String TAG = "[ApphudServiceModuleEntryPoint]";

    @Nullable
    private RemoteApphudConfig config;

    @NonNull
    private final RemoteApphudConfigBundleConverter bundleConverter = new RemoteApphudConfigBundleConverter();
    @NonNull
    private final RemoteApphudConfigParser configParser = new RemoteApphudConfigParser();
    @NonNull
    private final RemoteApphudConfigConverter configConverter = new RemoteApphudConfigConverter();
    @NonNull
    private final RemoteConfigUpdateListener<RemoteApphudConfig> configUpdateListener = config -> {
        synchronized(ApphudServiceModuleEntryPoint.this) {
            DebugLogger.INSTANCE.info(TAG, "received config " + config.getFeaturesConfig());
            ApphudServiceModuleEntryPoint.this.config = config.getFeaturesConfig();
        }
    };

    @NonNull
    @Override
    public String getIdentifier() {
        return Constants.MODULE_ID;
    }

    @Nullable
    @Override
    public RemoteConfigExtensionConfiguration<RemoteApphudConfig> getRemoteConfigExtensionConfiguration() {
        return new RemoteConfigExtensionConfiguration<RemoteApphudConfig>() {
            @NonNull
            @Override
            public RemoteConfigUpdateListener<RemoteApphudConfig> getRemoteConfigUpdateListener() {
                return configUpdateListener;
            }

            @NonNull
            @Override
            public Converter<RemoteApphudConfig, byte[]> getProtobufConverter() {
                return configConverter;
            }

            @NonNull
            @Override
            public JsonParser<RemoteApphudConfig> getJsonParser() {
                return configParser;
            }

            @NonNull
            @Override
            public Map<String, Integer> getBlocks() {
                return Map.of(
                    Constants.RemoteConfig.BLOCK_NAME_OBFUSCATED, Constants.RemoteConfig.BLOCK_VERSION
                );
            }

            @NonNull
            @Override
            public List<String> getFeatures() {
                return Collections.singletonList(
                    Constants.RemoteConfig.FEATURE_NAME_OBFUSCATED
                );
            }
        };
    }

    @Override
    public void initServiceSide(
        @NonNull ServiceContext serviceContext,
        @NonNull ModuleRemoteConfig<RemoteApphudConfig> initialConfig
    ) {
        config = initialConfig.getFeaturesConfig();
    }

    @Nullable
    @Override
    public ClientConfigProvider getClientConfigProvider() {
        return new ClientConfigProvider() {
            @Nullable
            @Override
            public Bundle getConfigBundleForClient() {
                DebugLogger.INSTANCE.info(TAG, "Converting config '" + config + "' to bundle");
                return bundleConverter.convert(config);
            }
        };
    }
}
