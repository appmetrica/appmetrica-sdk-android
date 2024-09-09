package io.appmetrica.analytics.apphud.impl;

import io.appmetrica.analytics.apphud.impl.protobuf.client.RemoteApphudConfigProtobuf;

public class Constants {
    public static final String MODULE_ID = "apphud";

    public static class Defaults {
        private static final RemoteApphudConfigProtobuf.RemoteApphudConfig defaultConfig =
            new RemoteApphudConfigProtobuf.RemoteApphudConfig();
        public static final boolean DEFAULT_FEATURE_STATE = defaultConfig.enabled;
        public static final String DEFAULT_API_KEY = defaultConfig.apiKey;
    }

    public static class RemoteConfig {
        public static final String BLOCK_NAME = "apphud";
        public static final String BLOCK_NAME_OBFUSCATED = "ah";
        public static final int BLOCK_VERSION = 1;

        public static final String FEATURE_NAME = "apphud";
        public static final String FEATURE_NAME_OBFUSCATED = "ah";

        public static final String API_KEY_KEY = "apikey";
    }

    public static class ServiceConfig {
        public static final String ENABLED_KEY = "enabled";
        public static final String API_KEY_KEY = "api_key";
    }

    public static class ClientConfig {
        public static final String API_KEY_KEY = "api_key";
        public static final String DEVICE_ID_KEY = "device_id";
        public static final String UUID_KEY = "uuid";
    }
}
