package io.appmetrica.analytics.billing.impl

internal object Constants {

    const val MODULE_NAME = "billing"

    internal object Events {

        const val TYPE = 40976
    }

    internal object RemoteConfig {
        const val BLOCK_NAME = "auto_inapp_collecting"
        const val BLOCK_NAME_OBFUSCATED = "aic"
        const val BLOCK_VERSION = 1

        const val SEND_FREQUENCY_SECONDS = "send_frequency_seconds"
        const val FIRST_COLLECTING_INAPP_MAX_AGE_SECONDS = "first_collecting_inapp_max_age_seconds"
    }

    internal object Storage {
        const val STORAGE_KEY = "auto_inapp_collecting_info_data"
    }
}
