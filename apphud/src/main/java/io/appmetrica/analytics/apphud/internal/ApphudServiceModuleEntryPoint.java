package io.appmetrica.analytics.apphud.internal;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import io.appmetrica.analytics.apphud.impl.Constants;
import io.appmetrica.analytics.apphud.impl.config.service.ServiceSideApphudConfigConverter;
import io.appmetrica.analytics.apphud.impl.config.service.ServiceSideApphudConfigParser;
import io.appmetrica.analytics.apphud.impl.config.service.ServiceSideApphudConfigToBundleConverter;
import io.appmetrica.analytics.apphud.impl.config.service.model.ServiceSideApphudConfig;
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
import org.json.JSONObject;

public class ApphudServiceModuleEntryPoint extends ModuleServiceEntryPoint<ServiceSideApphudConfigWrapper> {

    private static final String TAG = "[ApphudServiceModuleEntryPoint]";

    @Nullable
    private ServiceSideApphudConfig config;

    @NonNull
    private final ServiceSideApphudConfigToBundleConverter bundleConverter =
        new ServiceSideApphudConfigToBundleConverter();
    @NonNull
    private final ServiceSideApphudConfigParser configParser = new ServiceSideApphudConfigParser();
    @NonNull
    private final ServiceSideApphudConfigConverter configConverter = new ServiceSideApphudConfigConverter();
    @NonNull
    private final RemoteConfigUpdateListener<ServiceSideApphudConfigWrapper> configUpdateListener = moduleConfig -> {
        synchronized (ApphudServiceModuleEntryPoint.this) {
            ServiceSideApphudConfigWrapper wrapper = moduleConfig.getFeaturesConfig();
            ServiceSideApphudConfig config = wrapper == null ? null : wrapper.config;
            DebugLogger.INSTANCE.info(TAG, "received config " + config);
            ApphudServiceModuleEntryPoint.this.config = config;
        }
    };

    @NonNull
    private final JsonParser<ServiceSideApphudConfigWrapper> wrapperJsonParser =
        new JsonParser<ServiceSideApphudConfigWrapper>() {
            @NonNull
            @Override
            public ServiceSideApphudConfigWrapper parse(@NonNull JSONObject rawData) {
                return ServiceSideApphudConfigWrapper.toWrapper(configParser.parse(rawData));
            }

            @Nullable
            @Override
            public ServiceSideApphudConfigWrapper parseOrNull(@NonNull JSONObject rawData) {
                ServiceSideApphudConfig parsed = configParser.parseOrNull(rawData);
                return parsed == null ? null : ServiceSideApphudConfigWrapper.toWrapper(parsed);
            }
        };

    @NonNull
    private final Converter<ServiceSideApphudConfigWrapper, byte[]> wrapperProtobufConverter =
        new Converter<ServiceSideApphudConfigWrapper, byte[]>() {
            @Override
            public byte[] fromModel(@NonNull ServiceSideApphudConfigWrapper value) {
                return configConverter.fromModel(value.config);
            }

            @NonNull
            @Override
            public ServiceSideApphudConfigWrapper toModel(@NonNull byte[] value) {
                return ServiceSideApphudConfigWrapper.toWrapper(configConverter.toModel(value));
            }
        };

    @NonNull
    @Override
    public String getIdentifier() {
        return Constants.MODULE_ID;
    }

    @NonNull
    private final RemoteConfigExtensionConfiguration<ServiceSideApphudConfigWrapper> remoteExtensionConfiguration =
        new RemoteConfigExtensionConfiguration<ServiceSideApphudConfigWrapper>() {
            @NonNull
            @Override
            public List<String> getFeatures() {
                return Collections.singletonList(
                    Constants.RemoteConfig.FEATURE_NAME_OBFUSCATED
                );
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
            public JsonParser<ServiceSideApphudConfigWrapper> getJsonParser() {
                return wrapperJsonParser;
            }

            @NonNull
            @Override
            public Converter<ServiceSideApphudConfigWrapper, byte[]> getProtobufConverter() {
                return wrapperProtobufConverter;
            }

            @NonNull
            @Override
            public RemoteConfigUpdateListener<ServiceSideApphudConfigWrapper> getRemoteConfigUpdateListener() {
                return configUpdateListener;
            }
        };

    @NonNull
    private final ClientConfigProvider clientConfigProvider = new ClientConfigProvider() {
        @Nullable
        @Override
        public Bundle getConfigBundleForClient() {
            DebugLogger.INSTANCE.info(TAG, "Converting config '" + config + "' to bundle");
            return bundleConverter.convert(config);
        }
    };

    @Nullable
    @Override
    public RemoteConfigExtensionConfiguration<ServiceSideApphudConfigWrapper> getRemoteConfigExtensionConfiguration() {
        return remoteExtensionConfiguration;
    }

    @Override
    public void initServiceSide(
        @NonNull ServiceContext serviceContext,
        @NonNull ModuleRemoteConfig<ServiceSideApphudConfigWrapper> initialConfig
    ) {
        ServiceSideApphudConfigWrapper wrapper = initialConfig.getFeaturesConfig();
        config = wrapper == null ? null : wrapper.config;
    }

    @Nullable
    @Override
    public ClientConfigProvider getClientConfigProvider() {
        return clientConfigProvider;
    }
}
