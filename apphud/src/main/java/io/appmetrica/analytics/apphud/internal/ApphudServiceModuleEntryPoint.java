package io.appmetrica.analytics.apphud.internal;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.config.ModuleConfig;
import io.appmetrica.analytics.apphud.impl.config.ModuleConfigConverter;
import io.appmetrica.analytics.apphud.impl.config.ModuleConfigParser;
import io.appmetrica.analytics.apphud.impl.config.ModuleConfigToProtoConverter;
import io.appmetrica.analytics.coreapi.internal.data.Converter;
import io.appmetrica.analytics.coreapi.internal.data.JsonParser;
import io.appmetrica.analytics.modulesapi.internal.service.ClientConfigProvider;
import io.appmetrica.analytics.modulesapi.internal.service.ModuleRemoteConfig;
import io.appmetrica.analytics.modulesapi.internal.service.ModuleServiceEntryPoint;
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigExtensionConfiguration;
import io.appmetrica.analytics.modulesapi.internal.service.RemoteConfigUpdateListener;
import io.appmetrica.analytics.modulesapi.internal.service.ServiceContext;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class ApphudServiceModuleEntryPoint extends ModuleServiceEntryPoint<ModuleConfig> {

    @Nullable
    private ModuleRemoteConfig<ModuleConfig> config;

    @NonNull
    private final ModuleConfigToProtoConverter configToProtoConverter = new ModuleConfigToProtoConverter();
    @NonNull
    private final ModuleConfigParser configParser = new ModuleConfigParser(configToProtoConverter);
    @NonNull
    private final ModuleConfigConverter configConverter = new ModuleConfigConverter(configToProtoConverter);
    @NonNull
    private final RemoteConfigUpdateListener<ModuleConfig> configUpdateListener = config -> {
        synchronized(ApphudServiceModuleEntryPoint.this) {
            ApphudServiceModuleEntryPoint.this.config = config;
        }
    };

    @NonNull
    @Override
    public String getIdentifier() {
        return Constants.MODULE_ID;
    }

    @Nullable
    @Override
    public RemoteConfigExtensionConfiguration<ModuleConfig> getRemoteConfigExtensionConfiguration() {
        return new RemoteConfigExtensionConfiguration<ModuleConfig>() {
            @NonNull
            @Override
            public RemoteConfigUpdateListener<ModuleConfig> getRemoteConfigUpdateListener() {
                return configUpdateListener;
            }

            @NonNull
            @Override
            public Converter<ModuleConfig, byte[]> getProtobufConverter() {
                return configConverter;
            }

            @NonNull
            @Override
            public JsonParser<ModuleConfig> getJsonParser() {
                return configParser;
            }

            @NonNull
            @Override
            public Map<String, Integer> getBlocks() {
                return Map.of(
                    Constants.Startup.BLOCK_NAME_OBFUSCATED, Constants.Startup.BLOCK_VERSION
                );
            }

            @NonNull
            @Override
            public List<String> getFeatures() {
                return Collections.emptyList();
            }
        };
    }

    @Override
    public void initServiceSide(
        @NonNull ServiceContext serviceContext,
        @NonNull ModuleRemoteConfig<ModuleConfig> initialConfig
    ) {
        config = initialConfig;
    }

    @Nullable
    @Override
    public ClientConfigProvider getClientConfigProvider() {
        return new ClientConfigProvider() {
            @Nullable
            @Override
            public Bundle getConfigBundleForClient() {
                if (config == null) {
                    return null;
                }
                Bundle bundle = new Bundle();
                bundle.putString(Constants.Config.API_KEY_KEY, config.getFeaturesConfig().getApiKey());
                return bundle;
            }
        };
    }
}
