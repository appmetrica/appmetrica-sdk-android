package io.appmetrica.analytics.logger.appmetrica.internal

import io.appmetrica.analytics.logger.common.BaseReleaseLogger

class PublicLogger(partialApiKey: String) : BaseReleaseLogger("AppMetrica", "[$partialApiKey]") {

    private constructor() : this("")

    companion object {
        private val ANONYMOUS_INSTANCE: PublicLogger = PublicLogger()

        @JvmStatic
        fun getAnonymousInstance() = ANONYMOUS_INSTANCE
    }
}
